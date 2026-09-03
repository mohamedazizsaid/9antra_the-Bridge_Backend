package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.DTOHelper;
import com._antra.the_bridge.dto.NotificationDTO;
import com._antra.the_bridge.dto.OnboardingRequest;
import com._antra.the_bridge.dto.StageInscriptionDTO;
import com._antra.the_bridge.entity.Formation;
import com._antra.the_bridge.entity.Notification;
import com._antra.the_bridge.entity.StageInscription;
import com._antra.the_bridge.entity.User;
import com._antra.the_bridge.enumType.InternshipPaymentMode;
import com._antra.the_bridge.enumType.InternshipStatus;
import com._antra.the_bridge.enumType.ReferralStatus;
import com._antra.the_bridge.enumType.Role;
import com._antra.the_bridge.exception.CustomException;
import com._antra.the_bridge.repository.FormationRepository;
import com._antra.the_bridge.repository.NotificationRepository;
import com._antra.the_bridge.repository.StageInscriptionRepository;
import com._antra.the_bridge.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import com._antra.the_bridge.entity.ComboEnrollment;
import com._antra.the_bridge.entity.Enrollment;
import com._antra.the_bridge.entity.Payment;
import com._antra.the_bridge.entity.Phase;
import com._antra.the_bridge.enumType.EnrollmentStatus;
import com._antra.the_bridge.enumType.PaymentStatus;
import com._antra.the_bridge.repository.ComboEnrollmentRepository;
import com._antra.the_bridge.repository.EnrollmentRepository;
import com._antra.the_bridge.repository.PaymentRepository;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StageInscriptionServiceImpl implements StageInscriptionService {

    private final StageInscriptionRepository stageRepo;
    private final UserRepository userRepository;
    private final FormationRepository formationRepository;
    private final CloudinaryService cloudinaryService;
    private final MailService mailService;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final EnrollmentRepository enrollmentRepository;
    private final ComboEnrollmentRepository comboEnrollmentRepository;
    private final PaymentRepository paymentRepository;

    @Value("${stripe.api-key:sk_test_mock}")
    private String stripeApiKey;

    @Value("${stripe.success-url:http://localhost:4200/payment-success?session_id={CHECKOUT_SESSION_ID}}")
    private String stripeSuccessUrl;

    @Value("${stripe.cancel-url:http://localhost:4200/payment-fail}")
    private String stripeCancelUrl;

    public StageInscriptionServiceImpl(StageInscriptionRepository stageRepo,
            UserRepository userRepository,
            FormationRepository formationRepository,
            CloudinaryService cloudinaryService,
            MailService mailService,
            NotificationRepository notificationRepository,
            SimpMessagingTemplate messagingTemplate,
            EnrollmentRepository enrollmentRepository,
            ComboEnrollmentRepository comboEnrollmentRepository,
            PaymentRepository paymentRepository) {
        this.stageRepo = stageRepo;
        this.userRepository = userRepository;
        this.formationRepository = formationRepository;
        this.cloudinaryService = cloudinaryService;
        this.mailService = mailService;
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
        this.enrollmentRepository = enrollmentRepository;
        this.comboEnrollmentRepository = comboEnrollmentRepository;
        this.paymentRepository = paymentRepository;
    }

    // ─── SUBMIT ONBOARDING ────────────────────────────────────────────────────

    @Override
    @Transactional
    public StageInscriptionDTO submitOnboarding(int studentId, OnboardingRequest request,
            MultipartFile demande, MultipartFile lettre) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new CustomException("Stagiaire introuvable", HttpStatus.NOT_FOUND));

        List<Long> formationIds = request.getSelectedFormationIds() != null ? request.getSelectedFormationIds() : new ArrayList<>();
        List<Formation> formations = formationIds.isEmpty() ? new ArrayList<>() : formationRepository.findAllById(formationIds);

        InternshipPaymentMode paymentMode = null;
        if (request.getPaymentMode() != null) {
            try {
                paymentMode = InternshipPaymentMode.valueOf(request.getPaymentMode());
            } catch (IllegalArgumentException e) {
                throw new CustomException("Mode de paiement invalide: " + request.getPaymentMode(),
                        HttpStatus.BAD_REQUEST);
            }
        }

        // =========================================================================
        // CAS 1 : FORMATION SIMPLE UNIQUE (wantsInternship == false && 1 formation)
        // AUCUN stage n'est créé dans stage_inscription !
        // =========================================================================
        if (!request.isWantsInternship() && formations.size() == 1) {
            Formation formation = formations.get(0);

            Enrollment enrollment = enrollmentRepository.findByStudentIdAndFormationId(studentId, formation.getId())
                    .orElse(new Enrollment());
            enrollment.setStudent(student);
            enrollment.setFormation(formation);
            enrollment.setEnrollmentDate(LocalDate.now());
            enrollment.setStatus(EnrollmentStatus.APPROVED);
            Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

            // Générer les échéances de paiement
            generatePayments(savedEnrollment, formation);

            // Marquer l'utilisateur comme ayant complété son onboarding
            student.setOnboardingCompleted(true);
            userRepository.save(student);

            // Créer session Stripe si paiement immédiat en ligne demandé
            String stripeUrl = null;
            if (request.isPayNow() && paymentMode == InternshipPaymentMode.STRIPE && formation.getTotalPrice() != null && formation.getTotalPrice() > 0) {
                try {
                    stripeUrl = createStripeCheckoutSessionForSingleFormation(savedEnrollment, formation, student);
                } catch (Exception e) {
                    System.err.println("Stripe session creation warning: " + e.getMessage());
                }
            }

            // Notification stagiaire
            sendNotification(student, "🎉 Inscription confirmée",
                    "Votre inscription à la formation « " + formation.getTitle() + " » a été confirmée avec succès !");

            return StageInscriptionDTO.builder()
                    .id(savedEnrollment.getId())
                    .studentId(student.getId())
                    .studentFirstName(student.getFirstName())
                    .studentLastName(student.getLastName())
                    .studentEmail(student.getEmail())
                    .studentAvatar(student.getAvatar())
                    .studentCin(student.getCin())
                    .wantsInternship(false)
                    .selectedFormationIds(List.of(formation.getId()))
                    .selectedFormationTitles(List.of(formation.getTitle()))
                    .originalPrice(formation.getTotalPrice())
                    .totalPrice(formation.getTotalPrice())
                    .paymentMode(paymentMode)
                    .payNow(request.isPayNow())
                    .stripePaymentUrl(stripeUrl)
                    .onboardingCompleted(true)
                    .completedAt(LocalDate.now())
                    .createdAt(LocalDate.now())
                    .build();
        }

        // =========================================================================
        // CAS 2 : COMBO MULTI-FORMATIONS (wantsInternship == false && >= 2 formations)
        // AUCUN stage n'est créé dans stage_inscription !
        // =========================================================================
        if (!request.isWantsInternship() && formations.size() >= 2) {
            double totalPrice = formations.stream()
                    .mapToDouble(f -> f.getTotalPrice() != null ? f.getTotalPrice() : 0.0)
                    .sum();
            double discountPercent = ComboEnrollment.computeDiscountPercent(formations.size());
            double finalPrice = totalPrice * (1.0 - discountPercent / 100.0);

            String receiptRef = "BRG-COMBO-" +
                    LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-" +
                    UUID.randomUUID().toString().substring(0, 6).toUpperCase();

            ComboEnrollment combo = new ComboEnrollment();
            combo.setStudent(student);
            combo.setFormations(formations);
            combo.setTotalPrice(totalPrice);
            combo.setDiscountPercent(discountPercent);
            combo.setFinalPrice(finalPrice);
            combo.setReceiptRef(receiptRef);
            combo.setCreatedAt(LocalDate.now());
            // Toujours PENDING_PAYMENT tant qu'aucun règlement n'a été validé
            combo.setStatus("PENDING_PAYMENT");
            combo.setPaidAt(null);

            ComboEnrollment savedCombo = comboEnrollmentRepository.save(combo);

            // Générer les inscriptions aux formations du combo (sans créer d'échéances par phase)
            for (Formation f : formations) {
                Enrollment e = enrollmentRepository.findByStudentIdAndFormationId(studentId, f.getId())
                        .orElse(new Enrollment());
                e.setStudent(student);
                e.setFormation(f);
                e.setEnrollmentDate(LocalDate.now());
                e.setStatus(EnrollmentStatus.APPROVED);
                e.setComboEnrollment(savedCombo);
                Enrollment savedE = enrollmentRepository.save(e);

                // Pour un combo, le paiement est global : supprimer tout paiement résiduel par phase
                List<Payment> existingPayments = paymentRepository.findByEnrollmentId(savedE.getId());
                if (existingPayments != null && !existingPayments.isEmpty()) {
                    paymentRepository.deleteAll(existingPayments);
                }
            }

            // Session Stripe si paiement immédiat en ligne demandé
            String stripeUrl = null;
            if (request.isPayNow() && paymentMode == InternshipPaymentMode.STRIPE && finalPrice > 0) {
                try {
                    stripeUrl = createStripeCheckoutSessionForCombo(savedCombo, formations, finalPrice, student);
                    savedCombo.setStripeSessionId(extractSessionId(stripeUrl));
                    comboEnrollmentRepository.save(savedCombo);
                } catch (Exception e) {
                    System.err.println("Stripe combo session creation warning: " + e.getMessage());
                }
            }

            student.setOnboardingCompleted(true);
            userRepository.save(student);

            String notifMsg = request.isPayNow()
                    ? "Votre parcours combo de " + formations.size() + " formations a été enregistré avec " + Math.round(discountPercent) + "% de remise !"
                    : "Votre parcours combo de " + formations.size() + " formations a été enregistré. Vous pouvez le régler à tout moment depuis votre espace.";
            sendNotification(student, "🎁 Parcours Combo Enregistré", notifMsg);

            return StageInscriptionDTO.builder()
                    .id(savedCombo.getId())
                    .studentId(student.getId())
                    .studentFirstName(student.getFirstName())
                    .studentLastName(student.getLastName())
                    .studentEmail(student.getEmail())
                    .studentAvatar(student.getAvatar())
                    .studentCin(student.getCin())
                    .wantsInternship(false)
                    .selectedFormationIds(formations.stream().map(Formation::getId).collect(Collectors.toList()))
                    .selectedFormationTitles(formations.stream().map(Formation::getTitle).collect(Collectors.toList()))
                    .originalPrice(totalPrice)
                    .discountAmount(totalPrice - finalPrice)
                    .discountReason("Remise combo (" + Math.round(discountPercent) + "%)")
                    .totalPrice(finalPrice)
                    .paymentMode(paymentMode)
                    .payNow(request.isPayNow())
                    .stripePaymentUrl(stripeUrl)
                    .onboardingCompleted(true)
                    .completedAt(LocalDate.now())
                    .createdAt(LocalDate.now())
                    .build();
        }

        // =========================================================================
        // CAS 3 : STAGE FACULTATIF (wantsInternship == true)
        // SEULEMENT ICI un StageInscription est créé !
        // =========================================================================
        List<StageInscription> existingList = stageRepo.findAllByStudentIdOrderByCreatedAtDesc(studentId);
        boolean hasEngagedStage = existingList.stream()
                .anyMatch(i -> i.getStatus() == InternshipStatus.PENDING_REVIEW ||
                        i.getStatus() == InternshipStatus.APPROVED ||
                        i.getStatus() == InternshipStatus.ACTIVE);
        if (hasEngagedStage) {
            throw new CustomException(
                    "Vous avez déjà un stage en cours ou en attente de validation. Vous ne pouvez pas demander un autre stage actuellement.",
                    HttpStatus.CONFLICT);
        }

        StageInscription inscription = new StageInscription();
        inscription.setStudent(student);
        inscription.setWantsInternship(true);

        if (request.getStageProjectTitle() == null || request.getStageProjectTitle().isBlank()) {
            throw new CustomException("Le titre du projet de stage est obligatoire", HttpStatus.BAD_REQUEST);
        }
        inscription.setStageProjectTitle(request.getStageProjectTitle());
        inscription.setStageDurationWeeks(request.getStageDurationWeeks());

        if (demande != null && !demande.isEmpty()) {
            String demandeUrl = cloudinaryService.uploadStagePdf(demande, "demande-stage");
            inscription.setDemandeStageUrl(demandeUrl);
        }
        if (lettre != null && !lettre.isEmpty()) {
            String lettreUrl = cloudinaryService.uploadStagePdf(lettre, "lettre-affectation");
            inscription.setLettreAffectationUrl(lettreUrl);
        }

        inscription.setSelectedFormations(formations);

        double originalPrice = formations.stream()
                .mapToDouble(f -> f.getTotalPrice() != null ? f.getTotalPrice() : 0.0)
                .sum();
        inscription.setOriginalPrice(originalPrice);
        inscription.setPaymentMode(paymentMode);
        inscription.setPayNow(false); // Stage facultatif : pas de payNow direct avant approbation admin

        boolean cashDiscount = paymentMode == InternshipPaymentMode.COMPTANT;
        boolean referralDiscount = false;

        if (request.getReferralEmail() != null && !request.getReferralEmail().isBlank()) {
            inscription.setReferralEmail(request.getReferralEmail());
            inscription.setReferralStatus(ReferralStatus.PENDING);
            if (!cashDiscount) {
                referralDiscount = true;
                inscription.setReferralDiscountApplied(true);
            }
            sendReferralEmail(request.getReferralEmail(), student.getFirstName() + " " + student.getLastName());
        }

        double discountAmount = 0.0;
        String discountReason = null;
        if (cashDiscount) {
            discountAmount = originalPrice * 0.10;
            discountReason = "Remise paiement comptant (10%)";
            inscription.setCashDiscountApplied(true);
        } else if (referralDiscount) {
            discountAmount = originalPrice * 0.10;
            discountReason = "Remise parrainage (10%)";
        }
        inscription.setDiscountAmount(discountAmount);
        inscription.setDiscountReason(discountReason);
        inscription.setTotalPrice(originalPrice - discountAmount);

        inscription.setHeardFrom(request.getHeardFrom());
        inscription.setHeardFromOther(request.getHeardFromOther());
        inscription.setTermsAccepted(request.isTermsAccepted());
        if (request.isTermsAccepted()) {
            inscription.setTermsAcceptedAt(LocalDate.now());
        }

        inscription.setOnboardingCompleted(true);
        inscription.setCompletedAt(LocalDate.now());
        inscription.setStatus(InternshipStatus.PENDING_REVIEW);

        StageInscription saved = stageRepo.save(inscription);

        student.setOnboardingCompleted(true);
        userRepository.save(student);

        // Notifier les admins de la nouvelle demande de stage
        notifyAdminsNewStageApplication(saved, student);

        return DTOHelper.toStageInscriptionDTO(saved);
    }

    // ─── GET MY INSCRIPTION ───────────────────────────────────────────────────

    @Override
    public StageInscriptionDTO getMyInscription(int studentId) {
        List<StageInscription> list = stageRepo.findAllByStudentIdOrderByCreatedAtDesc(studentId);
        if (list.isEmpty()) {
            throw new CustomException("Aucune inscription trouvée pour ce stagiaire", HttpStatus.NOT_FOUND);
        }
        StageInscription inscription = list.stream()
                .filter(i -> i.getStatus() == InternshipStatus.ACTIVE || i.getStatus() == InternshipStatus.APPROVED
                        || i.getStatus() == InternshipStatus.PENDING_REVIEW)
                .findFirst()
                .orElse(list.get(0));
        return DTOHelper.toStageInscriptionDTO(inscription);
    }

    // ─── ADMIN: GET ALL ───────────────────────────────────────────────────────

    @Override
    public List<StageInscriptionDTO> getAllInscriptions() {
        return stageRepo.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "createdAt", "id")).stream()
                .map(DTOHelper::toStageInscriptionDTO)
                .collect(Collectors.toList());
    }

    // ─── ADMIN: GET BY ID ─────────────────────────────────────────────────────

    @Override
    public StageInscriptionDTO getInscriptionById(Long id) {
        StageInscription inscription = stageRepo.findById(id)
                .orElseThrow(() -> new CustomException("Inscription introuvable", HttpStatus.NOT_FOUND));
        return DTOHelper.toStageInscriptionDTO(inscription);
    }

    // ─── ADMIN: UPDATE STATUS & ASSIGN SUPERVISOR ───────────────────────────

    @Override
    @Transactional
    public StageInscriptionDTO updateInscriptionStatus(Long id, InternshipStatus status, String notes,
            Integer supervisorId) {
        StageInscription inscription = stageRepo.findById(id)
                .orElseThrow(() -> new CustomException("Inscription introuvable", HttpStatus.NOT_FOUND));

        InternshipStatus oldStatus = inscription.getStatus();
        inscription.setStatus(status);
        if (notes != null) {
            inscription.setAdminNotes(notes.trim());
        }

        User supervisor = null;
        if (supervisorId != null && supervisorId > 0) {
            supervisor = userRepository.findById(supervisorId)
                    .orElseThrow(() -> new CustomException("Formateur / Encadrant introuvable", HttpStatus.NOT_FOUND));
            inscription.setSupervisor(supervisor);
        }

        StageInscription saved = stageRepo.save(inscription);

        // ─── Notification en Temps Réel pour le STAGIAIRE ──────────────────────
        notifyStudentStatusUpdate(saved, oldStatus, status, notes);

        // ─── Notification pour l'ENCADRANT (si assigné et approuvé) ───────────
        if (supervisor != null && (status == InternshipStatus.APPROVED || status == InternshipStatus.ACTIVE)) {
            User student = saved.getStudent();
            createAndSendNotification(
                    supervisor,
                    "🎓 Nouveau stagiaire sous votre encadrement",
                    "Vous avez été désigné comme encadrant pour le projet de stage « " +
                            (saved.getStageProjectTitle() != null ? saved.getStageProjectTitle() : "Stage Facultatif") +
                            " » du stagiaire " +
                            (student != null ? student.getFirstName() + " " + student.getLastName() : "") + ".");
        }

        return DTOHelper.toStageInscriptionDTO(saved);
    }

    // ─── ADMIN: CONFIRM MANUAL PAYMENT ────────────────────────────────────────

    @Override
    @Transactional
    public StageInscriptionDTO confirmAdminPayment(Long id) {
        StageInscription inscription = stageRepo.findById(id)
                .orElseThrow(() -> new CustomException("Inscription introuvable", HttpStatus.NOT_FOUND));

        InternshipPaymentMode mode = inscription.getPaymentMode();
        if (mode != InternshipPaymentMode.MAIN_A_MAIN && mode != InternshipPaymentMode.BANQUE) {
            throw new CustomException(
                    "La confirmation manuelle n'est disponible que pour les paiements MAIN_A_MAIN ou BANQUE",
                    HttpStatus.BAD_REQUEST);
        }

        inscription.setAdminPaymentConfirmed(true);
        inscription.setAdminPaymentDate(LocalDate.now());
        if (inscription.getStatus() == InternshipStatus.PENDING_REVIEW) {
            inscription.setStatus(InternshipStatus.ACTIVE);
        }

        StageInscription saved = stageRepo.save(inscription);

        // Notifier le stagiaire
        createAndSendNotification(
                saved.getStudent(),
                "💳 Paiement Manuel Confirmé !",
                "Votre règlement de " + String.format("%.2f", saved.getTotalPrice()) +
                        " TND a été validé et encaissé avec succès par l'administration 9antra.");

        return DTOHelper.toStageInscriptionDTO(saved);
    }

    // ─── ADMIN: UPDATE PAYMENT STATUS (TOGGLE) ────────────────────────────────

    @Override
    @Transactional
    public StageInscriptionDTO updatePaymentStatus(Long id, boolean paid) {
        StageInscription inscription = stageRepo.findById(id)
                .orElseThrow(() -> new CustomException("Inscription introuvable", HttpStatus.NOT_FOUND));

        inscription.setAdminPaymentConfirmed(paid);
        inscription.setAdminPaymentDate(paid ? LocalDate.now() : null);

        StageInscription saved = stageRepo.save(inscription);

        if (paid) {
            createAndSendNotification(
                    saved.getStudent(),
                    "💳 Règlement validé",
                    "Votre paiement de " + String.format("%.2f", saved.getTotalPrice()) +
                            " TND pour votre convention de stage / formations a été enregistré.");
        }

        return DTOHelper.toStageInscriptionDTO(saved);
    }

    // ─── HELPER: STRIPE CHECKOUT SESSION ──────────────────────────────────────

    private String createStripeCheckoutSessionForStage(StageInscription inscription, List<Formation> formations,
            double finalPrice) {
        Stripe.apiKey = this.stripeApiKey;

        long amountInCents = Math.round(finalPrice * 100);
        if (amountInCents < 50)
            amountInCents = 50;

        String description = inscription.isWantsInternship()
                ? "Convention Stage Facultatif — " + inscription.getStageProjectTitle()
                : "Formations certifiantes (" + formations.size() + " module(s))";

        String delimiter = stripeSuccessUrl != null && stripeSuccessUrl.contains("?") ? "&" : "?";
        String successUrl = (stripeSuccessUrl != null ? stripeSuccessUrl : "http://localhost:4200/payment-success")
                + delimiter + "stageInscriptionId=" + inscription.getId();

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(stripeCancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("The Bridge — "
                                                                        + (inscription.isWantsInternship()
                                                                                ? "Stage Facultatif"
                                                                                : "Inscription Formation"))
                                                                .setDescription(description)
                                                                .build())
                                                .build())
                                .build())
                .putMetadata("stageInscriptionId", String.valueOf(inscription.getId()))
                .putMetadata("studentId", String.valueOf(inscription.getStudent().getId()))
                .build();

        try {
            Session session = Session.create(params);
            inscription.setStripeSessionId(session.getId());
            return session.getUrl();
        } catch (Exception e) {
            throw new CustomException("Erreur lors de la création de la session Stripe: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String createStripeCheckoutSessionForSingleFormation(Enrollment enrollment, Formation formation, User student) {
        Stripe.apiKey = this.stripeApiKey;
        double price = formation.getTotalPrice() != null ? formation.getTotalPrice() : 0.0;
        long amountInCents = Math.max(50, Math.round(price * 100));

        String delimiter = stripeSuccessUrl != null && stripeSuccessUrl.contains("?") ? "&" : "?";
        String successUrl = (stripeSuccessUrl != null ? stripeSuccessUrl : "http://localhost:4200/payment-success")
                + delimiter + "enrollmentId=" + enrollment.getId();

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(stripeCancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("The Bridge — Formation : " + formation.getTitle())
                                                                .setDescription("Inscription à la formation certifiante " + formation.getTitle())
                                                                .build())
                                                .build())
                                .build())
                .putMetadata("enrollmentId", String.valueOf(enrollment.getId()))
                .putMetadata("studentId", String.valueOf(student.getId()))
                .build();

        try {
            Session session = Session.create(params);
            return session.getUrl();
        } catch (Exception e) {
            throw new CustomException("Erreur Stripe : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String createStripeCheckoutSessionForCombo(ComboEnrollment combo, List<Formation> formations, double finalPrice, User student) {
        Stripe.apiKey = this.stripeApiKey;
        long amountInCents = Math.max(50, Math.round(finalPrice * 100));

        String delimiter = stripeSuccessUrl != null && stripeSuccessUrl.contains("?") ? "&" : "?";
        String successUrl = (stripeSuccessUrl != null ? stripeSuccessUrl : "http://localhost:4200/payment-success")
                + delimiter + "comboId=" + combo.getId();

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(stripeCancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("The Bridge — Parcours Combo (" + formations.size() + " formations)")
                                                                .setDescription("Reçu N° " + combo.getReceiptRef())
                                                                .build())
                                                .build())
                                .build())
                .putMetadata("comboId", String.valueOf(combo.getId()))
                .putMetadata("studentId", String.valueOf(student.getId()))
                .build();

        try {
            Session session = Session.create(params);
            return session.getUrl();
        } catch (Exception e) {
            throw new CustomException("Erreur Stripe : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String extractSessionId(String stripeUrl) {
        if (stripeUrl == null) return null;
        try {
            if (stripeUrl.contains("/c/pay/")) {
                String sub = stripeUrl.substring(stripeUrl.indexOf("/c/pay/") + 7);
                int hashIdx = sub.indexOf("#");
                return hashIdx > 0 ? sub.substring(0, hashIdx) : sub;
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private void generatePayments(Enrollment enrollment, Formation formation) {
        if (formation.getPhases() != null && !formation.getPhases().isEmpty()) {
            for (Phase phase : formation.getPhases()) {
                Payment payment = new Payment();
                payment.setEnrollment(enrollment);
                payment.setPhase(phase);
                payment.setAmount(phase.getPrice() != null ? phase.getPrice() : 0.0);
                payment.setStatus(PaymentStatus.PENDING);
                int delay = (phase.getPhaseOrder() * 15) - 10;
                payment.setDueDate(LocalDate.now().plusDays(Math.max(1, delay)));
                paymentRepository.save(payment);
            }
        }
    }

    private void sendNotification(User recipient, String title, String message) {
        try {
            Notification notification = new Notification();
            notification.setUser(recipient);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setReadStatus(false);
            notification.setCreatedAt(LocalDateTime.now());
            notificationRepository.save(notification);
        } catch (Exception e) {
            // Ne pas bloquer le flux principal si la notification échoue
        }
    }

    // ─── HELPER: REAL-TIME NOTIFICATIONS ──────────────────────────────────────

    private void notifyAdminsNewStageApplication(StageInscription inscription, User student) {
        try {
            List<User> admins = userRepository.findByRole(Role.ADMIN);
            String title = "📝 Nouvelle demande de stage facultatif";
            String msg = "Le stagiaire " + student.getFirstName() + " " + student.getLastName() +
                    " (CIN: " + (student.getCin() != null ? student.getCin() : "N/A") + ")" +
                    " a soumis une demande pour : « " +
                    (inscription.isWantsInternship() ? inscription.getStageProjectTitle() : "Formations certifiantes") +
                    " » (" + String.format("%.2f", inscription.getTotalPrice()) + " TND).";

            for (User admin : admins) {
                createAndSendNotification(admin, title, msg);
            }
        } catch (Exception e) {
            System.err.println("Failed to notify admins: " + e.getMessage());
        }
    }

    private void notifyStudentStatusUpdate(StageInscription inscription, InternshipStatus oldStatus,
            InternshipStatus newStatus, String notes) {
        User student = inscription.getStudent();
        if (student == null)
            return;

        String title;
        String message;

        if (newStatus == InternshipStatus.APPROVED || newStatus == InternshipStatus.ACTIVE) {
            String supervisorInfo = inscription.getSupervisor() != null
                    ? " Encadrant assigné : " + inscription.getSupervisor().getFirstName() + " "
                            + inscription.getSupervisor().getLastName() + "."
                    : "";
            title = "🎉 Demande de stage approuvée !";
            message = "Félicitations " + student.getFirstName() + " ! Votre convention de stage pour le projet « " +
                    inscription.getStageProjectTitle() + " » a été approuvée par l'administration 9antra." +
                    supervisorInfo +
                    (notes != null && !notes.isBlank() ? " Remarque : " + notes : "");
        } else if (newStatus == InternshipStatus.REJECTED) {
            title = "⚠️ Demande de stage refusée";
            message = "Votre demande de stage pour le projet « " + inscription.getStageProjectTitle() +
                    " » n'a pas été retenue." +
                    (notes != null && !notes.isBlank() ? " Motif : " + notes
                            : " Veuillez contacter l'administration 9antra pour plus d'informations.");
        } else if (newStatus == InternshipStatus.COMPLETED) {
            title = "🎓 Stage clôturé avec succès";
            message = "Votre stage pour le projet « " + inscription.getStageProjectTitle() +
                    " » est officiellement validé et clôturé.";
        } else {
            title = "ℹ️ Mise à jour de votre dossier de stage";
            message = "Le statut de votre demande de stage est maintenant : " + newStatus + "." +
                    (notes != null && !notes.isBlank() ? " Note : " + notes : "");
        }

        createAndSendNotification(student, title, message);
    }

    private void createAndSendNotification(User user, String title, String message) {
        if (user == null)
            return;
        try {
            Notification notification = new Notification();
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setReadStatus(false);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setUser(user);

            Notification saved = notificationRepository.save(notification);

            // STOMP WebSocket push
            NotificationDTO dto = DTOHelper.toDTO(saved);
            messagingTemplate.convertAndSend("/topic/notifications/" + user.getId(), dto);
            messagingTemplate.convertAndSend("/topic/notifications", dto);
        } catch (Exception e) {
            System.err.println("Failed to push notification: " + e.getMessage());
        }
    }

    // ─── FORMATEUR: GET ASSIGNED STAGES ───────────────────────────────────────

    @Override
    public List<StageInscriptionDTO> getInscriptionsBySupervisor(int supervisorId) {
        return stageRepo.findBySupervisorId(supervisorId).stream()
                .map(DTOHelper::toStageInscriptionDTO)
                .collect(Collectors.toList());
    }

    // ─── HELPER: SEND REFERRAL EMAIL ──────────────────────────────────────────

    private void sendReferralEmail(String referralEmail, String referrerName) {
        try {
            mailService.sendReferralEmail(referralEmail, referrerName);
        } catch (Exception e) {
            // Don't block onboarding if email fails
        }
    }

    // ─── ADMIN: CLÔTURER LE STAGE & GÉNÉRER ATTESTATION PDF ───────────────────

    @Override
    @Transactional
    public StageInscriptionDTO cloturerStage(Long id) {
        StageInscription inscription = stageRepo.findById(id)
                .orElseThrow(() -> new CustomException("Inscription introuvable", HttpStatus.NOT_FOUND));

        if (inscription.getStatus() != InternshipStatus.APPROVED
                && inscription.getStatus() != InternshipStatus.ACTIVE) {
            throw new CustomException(
                    "Le stage doit être APPROUVÉ ou ACTIF pour être clôturé. Statut actuel : "
                            + inscription.getStatus(),
                    HttpStatus.BAD_REQUEST);
        }

        // ─── Générer le PDF d'attestation avec PDFBox ─────────────────────────
        try {
            byte[] pdfBytes = generateAttestationPdf(inscription);
            String attestationUrl = cloudinaryService.uploadPdfBytes(pdfBytes, "attestation-stage");
            inscription.setAttestationPdfUrl(attestationUrl);
        } catch (Exception e) {
            System.err.println("Warning: PDF attestation generation failed: " + e.getMessage());
            // Don't block closure if PDF fails
        }

        // ─── Marquer le stage comme COMPLETED ────────────────────────────────
        InternshipStatus oldStatus = inscription.getStatus();
        inscription.setStatus(InternshipStatus.COMPLETED);
        inscription.setCompletedAt(LocalDate.now());
        StageInscription saved = stageRepo.save(inscription);

        // ─── Notifier le stagiaire ────────────────────────────────────────────
        notifyStudentStatusUpdate(saved, oldStatus, InternshipStatus.COMPLETED, null);

        return DTOHelper.toStageInscriptionDTO(saved);
    }

    /**
     * Génère un PDF d'attestation de stage professionnel avec Apache PDFBox.
     */
    private byte[] generateAttestationPdf(StageInscription inscription) throws Exception {
        org.apache.pdfbox.pdmodel.PDDocument document = new org.apache.pdfbox.pdmodel.PDDocument();
        org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage(
                org.apache.pdfbox.pdmodel.common.PDRectangle.A4);
        document.addPage(page);

        User student = inscription.getStudent();
        String fullName = student != null
                ? (student.getFirstName() + " " + student.getLastName()).toUpperCase()
                : "STAGIAIRE";
        String cin = student != null && student.getCin() != null ? student.getCin() : "N/A";
        String email = student != null ? student.getEmail() : "N/A";
        String projectTitle = inscription.getStageProjectTitle() != null ? inscription.getStageProjectTitle()
                : "Projet de Stage";
        int durationWeeks = inscription.getStageDurationWeeks() != null ? inscription.getStageDurationWeeks() : 12;
        String supervisor = inscription.getSupervisor() != null
                ? inscription.getSupervisor().getFirstName() + " " + inscription.getSupervisor().getLastName()
                : "L'équipe 9antra";
        String startDate = inscription.getCompletedAt() != null
                ? inscription.getCompletedAt().minusWeeks(durationWeeks).toString()
                : "N/A";
        String endDate = inscription.getCompletedAt() != null
                ? inscription.getCompletedAt().toString()
                : LocalDate.now().toString();
        String issueDate = LocalDate.now().toString();

        try (org.apache.pdfbox.pdmodel.PDPageContentStream cs = new org.apache.pdfbox.pdmodel.PDPageContentStream(
                document, page)) {

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float margin = 60;

            // ── Background ────────────────────────────────────────────────────
            cs.setNonStrokingColor(0.059f, 0.063f, 0.161f); // #0F1029 dark navy
            cs.addRect(0, 0, pageWidth, pageHeight);
            cs.fill();

            // ── Top accent bar ────────────────────────────────────────────────
            cs.setNonStrokingColor(0.776f, 0.153f, 0.380f); // #C62761 crimson
            cs.addRect(0, pageHeight - 12, pageWidth, 12);
            cs.fill();

            // ── Bottom accent bar ─────────────────────────────────────────────
            cs.setNonStrokingColor(0.961f, 0.651f, 0.137f); // #F5A623 gold
            cs.addRect(0, 0, pageWidth, 8);
            cs.fill();

            // ── Left side bar ─────────────────────────────────────────────────
            cs.setNonStrokingColor(0.776f, 0.153f, 0.380f);
            cs.addRect(0, 0, 8, pageHeight);
            cs.fill();

            // ── Title Section ─────────────────────────────────────────────────
            org.apache.pdfbox.pdmodel.font.PDFont fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            org.apache.pdfbox.pdmodel.font.PDFont fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            // Organisation name
            cs.beginText();
            cs.setFont(fontBold, 10);
            cs.setNonStrokingColor(0.961f, 0.651f, 0.137f); // gold
            cs.newLineAtOffset(margin, pageHeight - 55);
            cs.showText("9ANTRA FORMATION PROFESSIONNELLE");
            cs.endText();

            // Certificate title
            cs.beginText();
            cs.setFont(fontBold, 24);
            cs.setNonStrokingColor(1f, 1f, 1f);
            cs.newLineAtOffset(margin, pageHeight - 110);
            cs.showText("ATTESTATION DE STAGE");
            cs.endText();

            // Subtitle
            cs.beginText();
            cs.setFont(fontRegular, 11);
            cs.setNonStrokingColor(0.8f, 0.8f, 0.8f);
            cs.newLineAtOffset(margin, pageHeight - 132);
            cs.showText("Stage Facultatif - Convention Validée et Clôturée");
            cs.endText();

            // Divider line
            cs.setStrokingColor(0.776f, 0.153f, 0.380f);
            cs.setLineWidth(2);
            cs.moveTo(margin, pageHeight - 148);
            cs.lineTo(pageWidth - margin, pageHeight - 148);
            cs.stroke();

            // ── Introductory Paragraph ────────────────────────────────────────
            cs.beginText();
            cs.setFont(fontRegular, 11);
            cs.setNonStrokingColor(0.85f, 0.85f, 0.85f);
            cs.newLineAtOffset(margin, pageHeight - 175);
            cs.showText("Nous soussignés, l'organisme de formation 9antra, certifions que :");
            cs.endText();

            // ── Student Name ──────────────────────────────────────────────────
            cs.beginText();
            cs.setFont(fontBold, 20);
            cs.setNonStrokingColor(0.961f, 0.651f, 0.137f);
            cs.newLineAtOffset(margin, pageHeight - 218);
            cs.showText(fullName);
            cs.endText();

            // ── CIN ───────────────────────────────────────────────────────────
            cs.beginText();
            cs.setFont(fontRegular, 10);
            cs.setNonStrokingColor(0.65f, 0.65f, 0.65f);
            cs.newLineAtOffset(margin, pageHeight - 236);
            cs.showText("CIN : " + cin + "   |   Email : " + email);
            cs.endText();

            // Divider
            cs.setStrokingColor(0.2f, 0.2f, 0.3f);
            cs.setLineWidth(0.5f);
            cs.moveTo(margin, pageHeight - 250);
            cs.lineTo(pageWidth - margin, pageHeight - 250);
            cs.stroke();

            // ── Body Text ─────────────────────────────────────────────────────
            float bodyY = pageHeight - 278;
            String[] bodyLines = {
                    "a effectué un stage facultatif au sein de notre centre de formation 9antra,",
                    "dans le cadre du programme de formation professionnelle certifiante."
            };
            for (String line : bodyLines) {
                cs.beginText();
                cs.setFont(fontRegular, 11);
                cs.setNonStrokingColor(0.85f, 0.85f, 0.85f);
                cs.newLineAtOffset(margin, bodyY);
                cs.showText(line);
                cs.endText();
                bodyY -= 18;
            }

            // ── Info Cards ────────────────────────────────────────────────────
            float cardY = bodyY - 30;
            drawInfoRow(cs, fontBold, fontRegular, margin, cardY, "Projet de Stage :", projectTitle, pageWidth);
            cardY -= 40;
            drawInfoRow(cs, fontBold, fontRegular, margin, cardY, "Durée du Stage :",
                    durationWeeks + " semaines (" + String.format("%.1f", durationWeeks / 4.0) + " mois)", pageWidth);
            cardY -= 40;
            drawInfoRow(cs, fontBold, fontRegular, margin, cardY, "Période :", "Du " + startDate + " au " + endDate,
                    pageWidth);
            cardY -= 40;
            drawInfoRow(cs, fontBold, fontRegular, margin, cardY, "Encadrant :", supervisor, pageWidth);
            cardY -= 40;
            drawInfoRow(cs, fontBold, fontRegular, margin, cardY, "Date d'émission :", issueDate, pageWidth);

            // ── Conclusion ────────────────────────────────────────────────────
            float conclusionY = cardY - 50;
            cs.beginText();
            cs.setFont(fontRegular, 11);
            cs.setNonStrokingColor(0.85f, 0.85f, 0.85f);
            cs.newLineAtOffset(margin, conclusionY);
            cs.showText("Cette attestation est délivrée à l'intéressé(e) pour servir et valoir ce que de droit.");
            cs.endText();

            // ── Signature Block ───────────────────────────────────────────────
            float sigY = conclusionY - 60;
            // Left: Cachet
            cs.beginText();
            cs.setFont(fontBold, 10);
            cs.setNonStrokingColor(0.961f, 0.651f, 0.137f);
            cs.newLineAtOffset(margin, sigY);
            cs.showText("Cachet & Signature");
            cs.endText();
            cs.beginText();
            cs.setFont(fontRegular, 9);
            cs.setNonStrokingColor(0.6f, 0.6f, 0.6f);
            cs.newLineAtOffset(margin, sigY - 16);
            cs.showText("9antra Formation Professionnelle");
            cs.endText();
            // Sig line
            cs.setStrokingColor(0.3f, 0.3f, 0.4f);
            cs.setLineWidth(0.8f);
            cs.moveTo(margin, sigY - 35);
            cs.lineTo(margin + 150, sigY - 35);
            cs.stroke();

            // Right: Stagiaire
            float rightX = pageWidth - margin - 150;
            cs.beginText();
            cs.setFont(fontBold, 10);
            cs.setNonStrokingColor(0.961f, 0.651f, 0.137f);
            cs.newLineAtOffset(rightX, sigY);
            cs.showText("Signature du Stagiaire");
            cs.endText();
            cs.setStrokingColor(0.3f, 0.3f, 0.4f);
            cs.moveTo(rightX, sigY - 35);
            cs.lineTo(rightX + 150, sigY - 35);
            cs.stroke();

            // ── Footer ────────────────────────────────────────────────────────
            cs.beginText();
            cs.setFont(fontRegular, 8);
            cs.setNonStrokingColor(0.45f, 0.45f, 0.55f);
            cs.newLineAtOffset(margin, 25);
            cs.showText(
                    "The Bridge by 9antra | Formation Professionnelle Certifiante | www.9antra.tn | Document officiel N° STG-"
                            + inscription.getId());
            cs.endText();
        }

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        document.save(baos);
        document.close();
        return baos.toByteArray();
    }

    private void drawInfoRow(org.apache.pdfbox.pdmodel.PDPageContentStream cs,
            org.apache.pdfbox.pdmodel.font.PDFont fontBold,
            org.apache.pdfbox.pdmodel.font.PDFont fontRegular,
            float x, float y, String label, String value, float pageWidth) throws Exception {
        // Background rect
        cs.setNonStrokingColor(0.08f, 0.09f, 0.20f);
        cs.addRect(x, y - 12, pageWidth - 2 * x, 30);
        cs.fill();

        cs.beginText();
        cs.setFont(fontBold, 10);
        cs.setNonStrokingColor(0.776f, 0.153f, 0.380f);
        cs.newLineAtOffset(x + 10, y + 4);
        cs.showText(label);
        cs.endText();

        cs.beginText();
        cs.setFont(fontRegular, 10);
        cs.setNonStrokingColor(1f, 1f, 1f);
        cs.newLineAtOffset(x + 180, y + 4);
        // Truncate value if too long
        String displayValue = value != null && value.length() > 60 ? value.substring(0, 57) + "..." : value;
        cs.showText(displayValue != null ? displayValue : "N/A");
        cs.endText();
    }

    // ─── STAGIAIRE: HISTORIQUE DES STAGES ─────────────────────────────────────

    @Override
    public List<StageInscriptionDTO> getMyInscriptionHistory(int studentId) {
        return stageRepo.findAllByStudentIdOrderByCreatedAtAsc(studentId).stream()
                .map(DTOHelper::toStageInscriptionDTO)
                .collect(Collectors.toList());
    }

    // ─── STAGIAIRE: CRÉER SESSION STRIPE POUR STAGE APPROUVÉ ───────────────────

    @Override
    public StageInscriptionDTO createStripeCheckoutSession(Long stageInscriptionId) {
        StageInscription inscription = stageRepo.findById(stageInscriptionId)
                .orElseThrow(() -> new CustomException("Dossier de stage introuvable", HttpStatus.NOT_FOUND));

        List<Formation> formations = inscription.getSelectedFormations() != null
                ? inscription.getSelectedFormations()
                : java.util.Collections.emptyList();

        double price = inscription.getTotalPrice() != null ? inscription.getTotalPrice() : 0.0;
        String stripeUrl = createStripeCheckoutSessionForStage(inscription, formations, price);
        inscription.setStripePaymentUrl(stripeUrl);
        StageInscription saved = stageRepo.save(inscription);
        return DTOHelper.toStageInscriptionDTO(saved);
    }

    // ─── STAGIAIRE: VÉRIFIER PAIEMENT STRIPE POUR STAGE ───────────────────────

    @Override
    @Transactional
    public StageInscriptionDTO verifyStripePayment(String sessionId, Long stageInscriptionId) {
        StageInscription inscription = stageRepo.findById(stageInscriptionId)
                .orElseThrow(() -> new CustomException("Dossier de stage introuvable", HttpStatus.NOT_FOUND));

        if (inscription.isStripePaymentConfirmed()) {
            return DTOHelper.toStageInscriptionDTO(inscription);
        }

        Stripe.apiKey = this.stripeApiKey;
        try {
            Session session = Session.retrieve(sessionId);
            if (!"paid".equalsIgnoreCase(session.getPaymentStatus())) {
                throw new CustomException(
                        "Paiement Stripe non validé (statut : " + session.getPaymentStatus() + ")",
                        HttpStatus.BAD_REQUEST);
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException("Erreur Stripe : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        inscription.setStripePaymentConfirmed(true);
        inscription.setStripeSessionId(sessionId);
        if (inscription.getStatus() == InternshipStatus.APPROVED) {
            inscription.setStatus(InternshipStatus.ACTIVE);
        }
        StageInscription saved = stageRepo.save(inscription);

        try {
            User student = saved.getStudent();
            if (student != null) {
                createAndSendNotification(student, "💳 Paiement Stripe confirmé",
                        "Votre règlement de " + String.format("%.2f", saved.getTotalPrice() != null ? saved.getTotalPrice() : 0.0)
                                + " TND pour votre stage a été validé avec succès !");
            }
        } catch (Exception ignored) {}

        return DTOHelper.toStageInscriptionDTO(saved);
    }
}
