package com._antra.the_bridge.controller;

import com._antra.the_bridge.dto.PaymentDTO;
import com._antra.the_bridge.dto.StripeCheckoutResponse;
import com._antra.the_bridge.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Paiements", description = "Endpoints pour le suivi financier des formations")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    @Operation(summary = "Obtenir tous les paiements (Admin)")
    public ResponseEntity<List<PaymentDTO>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Paiements d'un stagiaire")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByStudent(@PathVariable int studentId) {
        return ResponseEntity.ok(paymentService.getPaymentsByStudent(studentId));
    }

    @GetMapping("/formation/{formationId}")
    @Operation(summary = "Paiements enregistrés pour une formation")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByFormation(@PathVariable Long formationId) {
        return ResponseEntity.ok(paymentService.getPaymentsByFormation(formationId));
    }

    @PostMapping
    @Operation(summary = "Enregistrer un paiement de phase")
    public ResponseEntity<PaymentDTO> savePayment(@RequestBody PaymentDTO paymentDTO) {
        return ResponseEntity.ok(paymentService.savePayment(paymentDTO));
    }

    @GetMapping("/student/{studentId}/retard")
    @Operation(summary = "Nombre d'échéances de paiement en retard pour un stagiaire")
    public ResponseEntity<Map<String, Integer>> getRetardCount(@PathVariable int studentId) {
        return ResponseEntity.ok(Map.of("count", paymentService.getRetardCount(studentId)));
    }

    @PostMapping("/stripe/create-checkout-session")
    @Operation(summary = "Créer une session de paiement Stripe Checkout")
    public ResponseEntity<StripeCheckoutResponse> createStripeCheckoutSession(@RequestBody PaymentDTO paymentDTO) {
        return ResponseEntity.ok(paymentService.createStripeCheckoutSession(paymentDTO));
    }

    @GetMapping("/stripe/verify")
    @Operation(summary = "Vérifier et valider un paiement Stripe Checkout")
    public ResponseEntity<PaymentDTO> verifyStripePayment(
            @RequestParam String sessionId,
            @RequestParam Long enrollmentId,
            @RequestParam Long phaseId) {
        return ResponseEntity.ok(paymentService.verifyStripePayment(sessionId, enrollmentId, phaseId));
    }

    @PostMapping("/stripe/webhook")
    @Operation(summary = "Webhook Stripe pour notifier les paiements complétés")
    public ResponseEntity<Void> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        paymentService.handleStripeWebhook(payload, sigHeader);
        return ResponseEntity.ok().build();
    }
}

