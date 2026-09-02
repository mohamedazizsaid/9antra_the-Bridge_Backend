package com._antra.the_bridge.controller;

import com._antra.the_bridge.dto.DTOHelper;
import com._antra.the_bridge.dto.OnboardingRequest;
import com._antra.the_bridge.dto.StageInscriptionDTO;
import com._antra.the_bridge.entity.User;
import com._antra.the_bridge.enumType.InternshipStatus;
import com._antra.the_bridge.exception.CustomException;
import com._antra.the_bridge.service.StageInscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com._antra.the_bridge.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class StageInscriptionController {

    private final StageInscriptionService stageInscriptionService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public StageInscriptionController(StageInscriptionService stageInscriptionService,
                                       UserRepository userRepository,
                                       ObjectMapper objectMapper) {
        this.stageInscriptionService = stageInscriptionService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    // ─── STAGIAIRE : Soumettre l'onboarding ───────────────────────────────────

    @PostMapping(value = "/api/onboarding/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StageInscriptionDTO> submitOnboarding(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "demande", required = false) MultipartFile demande,
            @RequestPart(value = "lettre", required = false) MultipartFile lettre,
            @AuthenticationPrincipal UserDetails userDetails) {

        User student = getAuthenticatedUser(userDetails);
        OnboardingRequest request;
        try {
            request = objectMapper.readValue(dataJson, OnboardingRequest.class);
        } catch (Exception e) {
            throw new CustomException("Format de données invalide", HttpStatus.BAD_REQUEST);
        }

        StageInscriptionDTO dto = stageInscriptionService.submitOnboarding(student.getId(), request, demande, lettre);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // ─── STAGIAIRE : Voir mon inscription ─────────────────────────────────────

    @GetMapping("/api/onboarding/my")
    public ResponseEntity<StageInscriptionDTO> getMyInscription(
            @AuthenticationPrincipal UserDetails userDetails) {
        User student = getAuthenticatedUser(userDetails);
        return ResponseEntity.ok(stageInscriptionService.getMyInscription(student.getId()));
    }

    // ─── ADMIN : Lister toutes les inscriptions ───────────────────────────────

    @GetMapping("/api/admin/stage-inscriptions")
    public ResponseEntity<List<StageInscriptionDTO>> getAllInscriptions() {
        return ResponseEntity.ok(stageInscriptionService.getAllInscriptions());
    }

    // ─── ADMIN : Voir une inscription par ID ──────────────────────────────────

    @GetMapping("/api/admin/stage-inscriptions/{id}")
    public ResponseEntity<StageInscriptionDTO> getInscriptionById(@PathVariable Long id) {
        return ResponseEntity.ok(stageInscriptionService.getInscriptionById(id));
    }

    // ─── ADMIN : Mettre à jour le statut & assigner un encadrant ─────────────

    @PutMapping("/api/admin/stage-inscriptions/{id}/status")
    public ResponseEntity<StageInscriptionDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        String statusStr = (String) body.get("status");
        String notes = (String) body.get("notes");
        Integer supervisorId = null;
        if (body.get("supervisorId") != null) {
            try {
                supervisorId = Integer.valueOf(body.get("supervisorId").toString());
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        InternshipStatus status;
        try {
            status = InternshipStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            throw new CustomException("Statut invalide: " + statusStr, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(stageInscriptionService.updateInscriptionStatus(id, status, notes, supervisorId));
    }

    // ─── ADMIN : Récupérer la liste des formateurs ───────────────────────────

    @GetMapping("/api/admin/formateurs")
    public ResponseEntity<List<com._antra.the_bridge.dto.UserDTO>> getFormateurs() {
        List<com._antra.the_bridge.dto.UserDTO> formateurs = userRepository.findByRole(com._antra.the_bridge.enumType.Role.FORMATEUR).stream()
                .map(DTOHelper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(formateurs);
    }

    // ─── ADMIN : Confirmer paiement manuel ────────────────────────────────────

    @PutMapping("/api/admin/stage-inscriptions/{id}/confirm-payment")
    public ResponseEntity<StageInscriptionDTO> confirmPayment(@PathVariable Long id) {
        return ResponseEntity.ok(stageInscriptionService.confirmAdminPayment(id));
    }

    // ─── ADMIN : Modifier statut paiement (Payé / Non payé) ───────────────────

    @PutMapping("/api/admin/stage-inscriptions/{id}/payment-status")
    public ResponseEntity<StageInscriptionDTO> updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        boolean paid = body.getOrDefault("paid", true);
        return ResponseEntity.ok(stageInscriptionService.updatePaymentStatus(id, paid));
    }

    // ─── FORMATEUR : Voir les stagiaires de stage facultatif assignés ────────

    @GetMapping("/api/formateur/stage-inscriptions")
    public ResponseEntity<List<StageInscriptionDTO>> getSupervisedInscriptions(
            @AuthenticationPrincipal UserDetails userDetails) {
        User formateur = getAuthenticatedUser(userDetails);
        return ResponseEntity.ok(stageInscriptionService.getInscriptionsBySupervisor(formateur.getId()));
    }

    // ─── ADMIN : Clôturer le stage & générer attestation PDF ──────────────────

    @PostMapping("/api/admin/stage-inscriptions/{id}/cloturer")
    public ResponseEntity<StageInscriptionDTO> cloturerStage(@PathVariable Long id) {
        return ResponseEntity.ok(stageInscriptionService.cloturerStage(id));
    }

    // ─── STAGIAIRE : Historique de toutes mes inscriptions ───────────────────

    @GetMapping("/api/onboarding/my/history")
    public ResponseEntity<List<StageInscriptionDTO>> getMyHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        User student = getAuthenticatedUser(userDetails);
        return ResponseEntity.ok(stageInscriptionService.getMyInscriptionHistory(student.getId()));
    }

    // ─── STAGIAIRE : Ouvrir / Générer session de paiement Stripe pour stage approuvé ──

    @PostMapping("/api/onboarding/stage/{id}/stripe-checkout")
    public ResponseEntity<StageInscriptionDTO> createStripeCheckoutSession(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(stageInscriptionService.createStripeCheckoutSession(id));
    }

    // ─── CALLBACK : Vérifier et confirmer le paiement Stripe d'un stage ────────

    @GetMapping("/api/onboarding/stage/stripe/verify")
    public ResponseEntity<StageInscriptionDTO> verifyStagePayment(
            @RequestParam String sessionId,
            @RequestParam Long stageInscriptionId) {
        return ResponseEntity.ok(stageInscriptionService.verifyStripePayment(sessionId, stageInscriptionId));
    }

    // ─── HELPER ───────────────────────────────────────────────────────────────

    private User getAuthenticatedUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException("Utilisateur authentifié introuvable", HttpStatus.UNAUTHORIZED));
    }
}
