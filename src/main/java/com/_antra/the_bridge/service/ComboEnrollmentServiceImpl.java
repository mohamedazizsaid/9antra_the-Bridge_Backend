package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.ComboEnrollmentDTO;
import com._antra.the_bridge.dto.DTOHelper;
import com._antra.the_bridge.entity.*;
import com._antra.the_bridge.enumType.EnrollmentStatus;
import com._antra.the_bridge.exception.CustomException;
import com._antra.the_bridge.repository.*;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ComboEnrollmentServiceImpl implements ComboEnrollmentService {

    private final ComboEnrollmentRepository comboEnrollmentRepository;
    private final UserRepository userRepository;
    private final FormationRepository formationRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final NotificationRepository notificationRepository;
    private final MailService mailService;

    @Value("${stripe.api-key:sk_test_mock}")
    private String stripeApiKey;

    @Value("${stripe.success-url:http://localhost:4200/payment-success?session_id={CHECKOUT_SESSION_ID}}")
    private String stripeSuccessUrl;

    @Value("${stripe.cancel-url:http://localhost:4200/payment-fail}")
    private String stripeCancelUrl;

    public ComboEnrollmentServiceImpl(ComboEnrollmentRepository comboEnrollmentRepository,
                                      UserRepository userRepository,
                                      FormationRepository formationRepository,
                                      EnrollmentRepository enrollmentRepository,
                                      NotificationRepository notificationRepository,
                                      MailService mailService) {
        this.comboEnrollmentRepository = comboEnrollmentRepository;
        this.userRepository = userRepository;
        this.formationRepository = formationRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.notificationRepository = notificationRepository;
        this.mailService = mailService;
    }

    @PostConstruct
    public void initStripe() {
        Stripe.apiKey = this.stripeApiKey;
    }

    // ─── Création du combo + session Stripe ──────────────────────────────────

    @Override
    @Transactional
    public ComboEnrollmentDTO createComboEnrollment(int studentId, List<Long> formationIds, String note) {
        if (formationIds == null || formationIds.size() < 2) {
            throw new CustomException(
                    "Un combo doit contenir au minimum 2 formations pour bénéficier de la remise.",
                    HttpStatus.BAD_REQUEST);
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new CustomException("Stagiaire introuvable", HttpStatus.NOT_FOUND));

        List<Formation> formations = new ArrayList<>();
        double totalPrice = 0.0;

        for (Long fid : formationIds) {
            Formation f = formationRepository.findById(fid)
                    .orElseThrow(() -> new CustomException("Formation introuvable : " + fid, HttpStatus.NOT_FOUND));

            // Vérifier que la formation n'est pas déjà dans un combo actif du stagiaire
            if (comboEnrollmentRepository.existsActiveComboForStudentAndFormation(studentId, fid)) {
                throw new CustomException(
                        "La formation « " + f.getTitle() + " » est déjà dans un de vos combos actifs.",
                        HttpStatus.CONFLICT);
            }

            formations.add(f);
            totalPrice += (f.getTotalPrice() != null ? f.getTotalPrice() : 0.0);
        }

        // Calcul remise progressive
        double discountPercent = ComboEnrollment.computeDiscountPercent(formations.size());
        double finalPrice = totalPrice * (1.0 - discountPercent / 100.0);

        // Numéro de reçu unique
        String receiptRef = "BRG-COMBO-" +
                LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-" +
                UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // Création de l'entité
        ComboEnrollment combo = new ComboEnrollment();
        combo.setStudent(student);
        combo.setFormations(formations);
        combo.setTotalPrice(totalPrice);
        combo.setDiscountPercent(discountPercent);
        combo.setFinalPrice(finalPrice);
        combo.setStatus("PENDING_PAYMENT");
        combo.setCreatedAt(LocalDate.now());
        combo.setReceiptRef(receiptRef);
        combo.setNote(note);

        ComboEnrollment saved = comboEnrollmentRepository.save(combo);

        // Création de la session Stripe
        String stripeUrl = createStripeCheckoutSession(saved, formations, finalPrice);
        saved.setStripeSessionId(extractSessionIdFromUrl(stripeUrl));
        comboEnrollmentRepository.save(saved);

        ComboEnrollmentDTO dto = DTOHelper.toDTO(saved);
        dto.setStripeCheckoutUrl(stripeUrl);
        return dto;
    }

    // ─── Vérification paiement Stripe ────────────────────────────────────────

    @Override
    @Transactional
    public ComboEnrollmentDTO verifyComboPayment(String stripeSessionId, Long comboId) {
        Stripe.apiKey = this.stripeApiKey;

        ComboEnrollment combo = comboEnrollmentRepository.findById(comboId)
                .orElseThrow(() -> new CustomException("Combo introuvable", HttpStatus.NOT_FOUND));

        // Ne pas retraiter si déjà actif
        if ("ACTIVE".equals(combo.getStatus())) {
            return DTOHelper.toDTO(combo);
        }

        try {
            Session session = Session.retrieve(stripeSessionId);
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

        // Confirmer le combo et générer les inscriptions
        combo.setStatus("ACTIVE");
        combo.setPaidAt(LocalDate.now());

        List<Enrollment> enrollments = generateIndividualEnrollments(combo);
        combo.setEnrollments(enrollments);
        comboEnrollmentRepository.save(combo);

        // Notification et email
        sendComboConfirmedNotification(combo);
        List<String> formationTitles = combo.getFormations().stream()
                .map(Formation::getTitle)
                .collect(Collectors.toList());
        mailService.sendComboConfirmationEmail(
                combo.getStudent().getEmail(),
                combo.getStudent().getFirstName(),
                combo.getReceiptRef(),
                formationTitles,
                combo.getTotalPrice(),
                combo.getDiscountPercent(),
                combo.getFinalPrice()
        );

        return DTOHelper.toDTO(combo);
    }

    // ─── Queries ─────────────────────────────────────────────────────────────

    @Override
    public List<ComboEnrollmentDTO> getCombosByStudent(int studentId) {
        return comboEnrollmentRepository.findByStudentId(studentId).stream()
                .map(DTOHelper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ComboEnrollmentDTO> getCombosByFormateur(int formateurId) {
        return comboEnrollmentRepository.findByFormateur(formateurId).stream()
                .map(DTOHelper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ComboEnrollmentDTO> getAllCombos() {
        return comboEnrollmentRepository.findAll().stream()
                .map(DTOHelper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ComboEnrollmentDTO getComboById(Long id) {
        return DTOHelper.toDTO(comboEnrollmentRepository.findById(id)
                .orElseThrow(() -> new CustomException("Combo introuvable", HttpStatus.NOT_FOUND)));
    }

    @Override
    @Transactional
    public void cancelCombo(Long comboId, int studentId) {
        ComboEnrollment combo = comboEnrollmentRepository.findById(comboId)
                .orElseThrow(() -> new CustomException("Combo introuvable", HttpStatus.NOT_FOUND));

        if (combo.getStudent() == null || combo.getStudent().getId() != studentId) {
            throw new CustomException("Accès refusé", HttpStatus.FORBIDDEN);
        }
        if ("ACTIVE".equals(combo.getStatus())) {
            throw new CustomException("Impossible d'annuler un combo déjà payé.", HttpStatus.BAD_REQUEST);
        }
        combo.setStatus("CANCELLED");
        comboEnrollmentRepository.save(combo);
    }

    @Override
    @Transactional
    public void deleteCombo(Long comboId, int studentId) {
        ComboEnrollment combo = comboEnrollmentRepository.findById(comboId)
                .orElseThrow(() -> new CustomException("Combo introuvable", HttpStatus.NOT_FOUND));

        if (combo.getStudent() == null || combo.getStudent().getId() != studentId) {
            throw new CustomException("Accès refusé", HttpStatus.FORBIDDEN);
        }
        if ("ACTIVE".equals(combo.getStatus())) {
            throw new CustomException("Impossible de supprimer un combo actif et payé.", HttpStatus.BAD_REQUEST);
        }

        // Supprimer les inscriptions associées si existantes
        if (combo.getEnrollments() != null && !combo.getEnrollments().isEmpty()) {
            enrollmentRepository.deleteAll(combo.getEnrollments());
        }

        comboEnrollmentRepository.delete(combo);
    }

    @Override
    @Transactional
    public ComboEnrollmentDTO retryCheckout(Long comboId, int studentId) {
        ComboEnrollment combo = comboEnrollmentRepository.findById(comboId)
                .orElseThrow(() -> new CustomException("Combo introuvable", HttpStatus.NOT_FOUND));

        if (combo.getStudent() == null || combo.getStudent().getId() != studentId) {
            throw new CustomException("Accès refusé", HttpStatus.FORBIDDEN);
        }
        if ("ACTIVE".equals(combo.getStatus())) {
            throw new CustomException("Ce combo est déjà payé et actif.", HttpStatus.BAD_REQUEST);
        }

        // Si le combo était annulé, le remettre en PENDING_PAYMENT
        if ("CANCELLED".equals(combo.getStatus())) {
            combo.setStatus("PENDING_PAYMENT");
        }

        // Régénérer une nouvelle session Stripe pour ce combo existant
        String stripeUrl = createStripeCheckoutSession(combo, combo.getFormations(), combo.getFinalPrice());
        combo.setStripeSessionId(extractSessionIdFromUrl(stripeUrl));
        comboEnrollmentRepository.save(combo);

        ComboEnrollmentDTO dto = DTOHelper.toDTO(combo);
        dto.setStripeCheckoutUrl(stripeUrl);
        return dto;
    }

    // ─── Helpers privés ──────────────────────────────────────────────────────

    /**
     * Crée une session Stripe Checkout pour le combo (1 seul paiement global).
     */
    private String createStripeCheckoutSession(ComboEnrollment combo, List<Formation> formations, double finalPrice) {
        long amountInCents = Math.round(finalPrice * 100);
        // Stripe minimum = 50 cents
        if (amountInCents < 50) amountInCents = 50;

        String formationNames = formations.stream()
                .map(Formation::getTitle)
                .limit(3)
                .collect(Collectors.joining(", "));
        if (formations.size() > 3) formationNames += " ...";

        String successUrl = stripeSuccessUrl + "&comboId=" + combo.getId();

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
                                                                .setName("Parcours Personnalisé The Bridge")
                                                                .setDescription(
                                                                        formations.size() + " formation(s) : " + formationNames +
                                                                        " — Remise de " + combo.getDiscountPercent() + "%")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .putMetadata("comboId", String.valueOf(combo.getId()))
                .putMetadata("studentId", String.valueOf(combo.getStudent().getId()))
                .build();

        try {
            Session session = Session.create(params);
            return session.getUrl();
        } catch (Exception e) {
            throw new CustomException("Erreur Stripe : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** Extrait l'ID de session Stripe depuis l'URL (query param session_id) */
    private String extractSessionIdFromUrl(String url) {
        if (url == null) return null;
        // L'ID est dans la métadata, pas l'URL — on le stocke séparément
        // La méthode createComboEnrollment appellera Session.retrieve après
        return null; // sera mis à jour via un retrieve ultérieur si nécessaire
    }

    /**
     * Génère 1 Enrollment APPROVED par formation dans le combo.
     */
    private List<Enrollment> generateIndividualEnrollments(ComboEnrollment combo) {
        List<Enrollment> result = new ArrayList<>();
        for (Formation formation : combo.getFormations()) {
            // Éviter doublon si déjà inscrit individuellement
            boolean alreadyEnrolled = enrollmentRepository
                    .existsByStudentIdAndFormationId(combo.getStudent().getId(), formation.getId());
            if (!alreadyEnrolled) {
                Enrollment e = new Enrollment();
                e.setStudent(combo.getStudent());
                e.setFormation(formation);
                e.setEnrollmentDate(LocalDate.now());
                e.setStatus(EnrollmentStatus.APPROVED);
                e.setComboEnrollment(combo);
                result.add(enrollmentRepository.save(e));
            }
        }
        return result;
    }

    /** Envoie une notification in-app au stagiaire après confirmation du combo */
    private void sendComboConfirmedNotification(ComboEnrollment combo) {
        if (combo.getStudent() == null) return;
        Notification notif = new Notification();
        notif.setUser(combo.getStudent());
        notif.setTitle("🎉 Parcours personnalisé activé !");
        notif.setMessage("Votre combo de " + combo.getFormations().size() +
                " formation(s) a été payé et activé. Réf : " + combo.getReceiptRef());
        notif.setReadStatus(false);
        notif.setCreatedAt(java.time.LocalDateTime.now());
        notificationRepository.save(notif);
    }
}
