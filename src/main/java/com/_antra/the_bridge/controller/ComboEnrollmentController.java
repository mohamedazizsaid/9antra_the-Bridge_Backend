package com._antra.the_bridge.controller;

import com._antra.the_bridge.dto.ComboEnrollmentDTO;
import com._antra.the_bridge.service.ComboEnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/combo-enrollments")
@Tag(name = "Combos", description = "Endpoints pour le parcours personnalisé (combo de formations)")
public class ComboEnrollmentController {

    private final ComboEnrollmentService comboEnrollmentService;

    public ComboEnrollmentController(ComboEnrollmentService comboEnrollmentService) {
        this.comboEnrollmentService = comboEnrollmentService;
    }

    /**
     * Crée un nouveau combo et retourne l'URL Stripe Checkout.
     * Corps : { studentId, formationIds: [id1, id2, ...], note? }
     */
    @PostMapping
    @Operation(summary = "Créer un parcours personnalisé (combo) et générer la session Stripe")
    public ResponseEntity<ComboEnrollmentDTO> createCombo(@RequestBody Map<String, Object> body) {
        int studentId = ((Number) body.get("studentId")).intValue();

        @SuppressWarnings("unchecked")
        List<Number> rawIds = (List<Number>) body.get("formationIds");
        List<Long> formationIds = rawIds.stream().map(Number::longValue).toList();

        String note = body.containsKey("note") ? (String) body.get("note") : null;

        ComboEnrollmentDTO dto = comboEnrollmentService.createComboEnrollment(studentId, formationIds, note);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * Vérifie et confirme le paiement Stripe d'un combo.
     * Params : sessionId (Stripe), comboId
     */
    @GetMapping("/stripe/verify")
    @Operation(summary = "Vérifier et confirmer le paiement Stripe d'un combo")
    public ResponseEntity<ComboEnrollmentDTO> verifyComboPayment(
            @RequestParam String sessionId,
            @RequestParam Long comboId) {
        return ResponseEntity.ok(comboEnrollmentService.verifyComboPayment(sessionId, comboId));
    }

    /** Tous les combos d'un stagiaire */
    @GetMapping("/student/{studentId}")
    @Operation(summary = "Combos d'un stagiaire")
    public ResponseEntity<List<ComboEnrollmentDTO>> getCombosByStudent(@PathVariable int studentId) {
        return ResponseEntity.ok(comboEnrollmentService.getCombosByStudent(studentId));
    }

    /** Combos impliquant les formations d'un formateur (vue formateur) */
    @GetMapping("/formateur/{formateurId}")
    @Operation(summary = "Combos impliquant les formations d'un formateur")
    public ResponseEntity<List<ComboEnrollmentDTO>> getCombosByFormateur(@PathVariable int formateurId) {
        return ResponseEntity.ok(comboEnrollmentService.getCombosByFormateur(formateurId));
    }

    /** Tous les combos — supervision admin */
    @GetMapping
    @Operation(summary = "Supervision globale de tous les combos (Admin)")
    public ResponseEntity<List<ComboEnrollmentDTO>> getAllCombos() {
        return ResponseEntity.ok(comboEnrollmentService.getAllCombos());
    }

    /** Détail d'un combo */
    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un combo par ID")
    public ResponseEntity<ComboEnrollmentDTO> getComboById(@PathVariable Long id) {
        return ResponseEntity.ok(comboEnrollmentService.getComboById(id));
    }

    /**
     * Annuler un combo en PENDING_PAYMENT (avant paiement).
     * Corps : { studentId }
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Annuler un combo non encore payé")
    public ResponseEntity<Void> cancelCombo(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        int studentId = ((Number) body.get("studentId")).intValue();
        comboEnrollmentService.cancelCombo(id, studentId);
        return ResponseEntity.ok().build();
    }

    /**
     * Supprimer définitivement un combo annulé ou non payé.
     */
    @DeleteMapping("/{id}/delete")
    @Operation(summary = "Supprimer définitivement un combo annulé ou non payé")
    public ResponseEntity<Void> deleteCombo(
            @PathVariable Long id,
            @RequestParam int studentId) {
        comboEnrollmentService.deleteCombo(id, studentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Régénérer une session Stripe pour un combo existant en attente de paiement.
     */
    @PostMapping("/{id}/retry-checkout")
    @Operation(summary = "Régénérer une session Stripe pour un combo existant en attente")
    public ResponseEntity<ComboEnrollmentDTO> retryCheckout(
            @PathVariable Long id,
            @RequestParam int studentId) {
        return ResponseEntity.ok(comboEnrollmentService.retryCheckout(id, studentId));
    }
}
