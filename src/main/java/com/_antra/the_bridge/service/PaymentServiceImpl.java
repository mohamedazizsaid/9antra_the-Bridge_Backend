package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.DTOHelper;
import com._antra.the_bridge.dto.PaymentDTO;
import com._antra.the_bridge.dto.StripeCheckoutResponse;
import com._antra.the_bridge.entity.Enrollment;
import com._antra.the_bridge.entity.Payment;
import com._antra.the_bridge.entity.Phase;
import com._antra.the_bridge.enumType.PaymentStatus;
import com._antra.the_bridge.exception.CustomException;
import com._antra.the_bridge.repository.EnrollmentRepository;
import com._antra.the_bridge.repository.PaymentRepository;
import com._antra.the_bridge.repository.PhaseRepository;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PhaseRepository phaseRepository;
    private final ProgressionService progressionService;

    @Value("${stripe.api-key:sk_test_mock}")
    private String stripeApiKey;

    @Value("${stripe.success-url:http://localhost:4200/payment-success?session_id={CHECKOUT_SESSION_ID}}")
    private String stripeSuccessUrl;

    @Value("${stripe.cancel-url:http://localhost:4200/payment-fail}")
    private String stripeCancelUrl;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              EnrollmentRepository enrollmentRepository,
                              PhaseRepository phaseRepository,
                              ProgressionService progressionService) {
        this.paymentRepository = paymentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.phaseRepository = phaseRepository;
        this.progressionService = progressionService;
    }

    @PostConstruct
    public void initStripe() {
        Stripe.apiKey = this.stripeApiKey;
    }

    @Override
    public List<PaymentDTO> getPaymentsByStudent(int studentId) {
        return paymentRepository.findByStudentId(studentId).stream()
                .map(DTOHelper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDTO> getPaymentsByFormation(Long formationId) {
        return paymentRepository.findByFormationId(formationId).stream()
                .map(DTOHelper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentDTO savePayment(PaymentDTO paymentDTO) {
        Enrollment enrollment = enrollmentRepository.findById(paymentDTO.getEnrollmentId())
                .orElseThrow(() -> new CustomException("Enrollment not found", HttpStatus.NOT_FOUND));
        Phase phase = phaseRepository.findById(paymentDTO.getPhaseId())
                .orElseThrow(() -> new CustomException("Phase not found", HttpStatus.NOT_FOUND));

        Payment payment = new Payment();
        payment.setAmount(paymentDTO.getAmount());
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMethod(paymentDTO.getPaymentMethod() != null ? paymentDTO.getPaymentMethod() : "Stripe");
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionReference(paymentDTO.getTransactionReference());
        payment.setReceiptUrl(paymentDTO.getReceiptUrl());
        payment.setEnrollment(enrollment);
        payment.setPhase(phase);

        paymentRepository.save(payment);

        if (enrollment.getStudent() != null) {
            progressionService.checkAndUpdateProgress(enrollment.getStudent().getId(), phase.getId());
        }

        return DTOHelper.toDTO(payment);
    }

    @Override
    public int getRetardCount(int studentId) {
        List<Payment> payments = paymentRepository.findByStudentId(studentId);
        int count = 0;
        for (Payment payment : payments) {
            if (payment.getStatus() == PaymentStatus.PENDING) {
                count++;
            }
        }
        return count;
    }

    @Override
    public StripeCheckoutResponse createStripeCheckoutSession(PaymentDTO paymentDTO) {
        Stripe.apiKey = this.stripeApiKey;
        long amountInCents = Math.round(paymentDTO.getAmount() * 100);

        String successUrlWithParams = stripeSuccessUrl + "&enrollmentId=" + paymentDTO.getEnrollmentId() + "&phaseId=" + paymentDTO.getPhaseId();

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrlWithParams)
                .setCancelUrl(stripeCancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur") // ou "usd" ou "tnd" selon support
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Règlement Phase " + paymentDTO.getPhaseId())
                                                                .setDescription("Paiement de formation via Stripe")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .putMetadata("enrollmentId", String.valueOf(paymentDTO.getEnrollmentId()))
                .putMetadata("phaseId", String.valueOf(paymentDTO.getPhaseId()))
                .build();

        try {
            Session session = Session.create(params);
            return new StripeCheckoutResponse(session.getId(), session.getUrl(), session.getPaymentStatus());
        } catch (Exception e) {
            throw new CustomException("Erreur lors de la création de la session Stripe: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Value("${stripe.webhook-secret:}")
    private String stripeWebhookSecret;

    @Override
    public PaymentDTO verifyStripePayment(String sessionId, Long enrollmentId, Long phaseId) {
        Stripe.apiKey = this.stripeApiKey;

        try {
            Session session = Session.retrieve(sessionId);

            if ("paid".equalsIgnoreCase(session.getPaymentStatus())) {
                return processSuccessfulPayment(session, enrollmentId, phaseId);
            } else {
                throw new CustomException("Le paiement Stripe n'a pas été validé (Statut: " + session.getPaymentStatus() + ")", HttpStatus.BAD_REQUEST);
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException("Erreur lors de la vérification du paiement Stripe: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void handleStripeWebhook(String payload, String sigHeader) {
        com.stripe.model.Event event;
        try {
            if (sigHeader != null && stripeWebhookSecret != null && !stripeWebhookSecret.isBlank()) {
                event = com.stripe.net.Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
            } else {
                event = com.stripe.net.Webhook.constructEvent(payload, sigHeader, "");
            }
        } catch (Exception e) {
            throw new CustomException("Signature ou Webhook invalide: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }



        if ("checkout.session.completed".equals(event.getType())) {
            com.stripe.model.EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            if (dataObjectDeserializer.getObject().isPresent()) {
                Session session = (Session) dataObjectDeserializer.getObject().get();
                if (session.getMetadata() != null) {
                    String enrollmentIdStr = session.getMetadata().get("enrollmentId");
                    String phaseIdStr = session.getMetadata().get("phaseId");
                    if (enrollmentIdStr != null && phaseIdStr != null) {
                        Long enrollmentId = Long.parseLong(enrollmentIdStr);
                        Long phaseId = Long.parseLong(phaseIdStr);
                        processSuccessfulPayment(session, enrollmentId, phaseId);
                    }
                }
            }
        }
    }

    private PaymentDTO processSuccessfulPayment(Session session, Long enrollmentId, Long phaseId) {
        // Éviter les doublons si le paiement a déjà été enregistré via vérification synchrone ou webhook
        boolean exists = paymentRepository.findAll().stream()
                .anyMatch(p -> session.getId().equals(p.getTransactionReference()));

        if (exists) {
            Payment existing = paymentRepository.findAll().stream()
                    .filter(p -> session.getId().equals(p.getTransactionReference()))
                    .findFirst().orElse(null);
            return DTOHelper.toDTO(existing);
        }

        // Try param enrollmentId, then metadata, then fallback to first available enrollment
        Enrollment enrollment = (enrollmentId != null && enrollmentId > 0) ? enrollmentRepository.findById(enrollmentId).orElse(null) : null;
        if (enrollment == null && session.getMetadata() != null && session.getMetadata().get("enrollmentId") != null) {
            try {
                Long metaEnrollId = Long.parseLong(session.getMetadata().get("enrollmentId"));
                enrollment = enrollmentRepository.findById(metaEnrollId).orElse(null);
            } catch (Exception ignored) {}
        }
        if (enrollment == null) {
            List<Enrollment> allEnrollments = enrollmentRepository.findAll();
            if (!allEnrollments.isEmpty()) {
                enrollment = allEnrollments.get(0);
            } else {
                throw new CustomException("Aucune inscription trouvée pour enregistrer le paiement", HttpStatus.NOT_FOUND);
            }
        }

        // Try param phaseId, then metadata, then fallback to first available phase
        Phase phase = (phaseId != null && phaseId > 0) ? phaseRepository.findById(phaseId).orElse(null) : null;
        if (phase == null && session.getMetadata() != null && session.getMetadata().get("phaseId") != null) {
            try {
                Long metaPhaseId = Long.parseLong(session.getMetadata().get("phaseId"));
                phase = phaseRepository.findById(metaPhaseId).orElse(null);
            } catch (Exception ignored) {}
        }
        if (phase == null) {
            List<Phase> allPhases = phaseRepository.findAll();
            if (!allPhases.isEmpty()) {
                phase = allPhases.get(0);
            } else {
                throw new CustomException("Aucune phase trouvée pour enregistrer le paiement", HttpStatus.NOT_FOUND);
            }
        }

        Payment payment = new Payment();
        double amountPaid = session.getAmountTotal() != null ? session.getAmountTotal() / 100.0 : (phase.getPrice() != null ? phase.getPrice() : 0.0);
        payment.setAmount(amountPaid);
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMethod("Stripe");
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionReference(session.getId());
        payment.setEnrollment(enrollment);
        payment.setPhase(phase);

        Payment savedPayment = paymentRepository.save(payment);

        if (enrollment.getStudent() != null) {
            progressionService.checkAndUpdateProgress(enrollment.getStudent().getId(), phase.getId());
        }

        return DTOHelper.toDTO(savedPayment);
    }
}

