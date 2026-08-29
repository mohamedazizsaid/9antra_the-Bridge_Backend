package com._antra.the_bridge.controller;

import com._antra.the_bridge.dto.EnrollmentDTO;
import com._antra.the_bridge.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
@Tag(name = "Inscriptions", description = "Endpoints pour gérer les inscriptions aux formations")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    /**
     * Inscription à une formation.
     * Corps : { studentId, formationId, customDurationWeeks?, motivationMessage? }
     *  - Si customDurationWeeks est null/absent → parcours standard (APPROVED immédiat)
     *  - Si customDurationWeeks est fourni → durée custom (PENDING, notif formateur)
     */
    @PostMapping
    @Operation(summary = "Inscrire un stagiaire à une formation (avec ou sans durée personnalisée)")
    public ResponseEntity<EnrollmentDTO> enrollStudent(@RequestBody Map<String, Object> body) {
        int studentId = ((Number) body.get("studentId")).intValue();
        long formationId = ((Number) body.get("formationId")).longValue();
        Integer customDurationWeeks = body.containsKey("customDurationWeeks") && body.get("customDurationWeeks") != null
                ? ((Number) body.get("customDurationWeeks")).intValue()
                : null;
        String motivationMessage = body.containsKey("motivationMessage")
                ? (String) body.get("motivationMessage")
                : null;

        return ResponseEntity.ok(enrollmentService.enrollStudentWithOptions(
                studentId, formationId, customDurationWeeks, motivationMessage));
    }

    /**
     * Réponse du formateur à une demande d'inscription custom.
     * Corps : { approved: boolean, rejectionReason?: string }
     */
    @PutMapping("/{id}/respond")
    @Operation(summary = "Formateur approuve ou rejette une demande d'inscription")
    public ResponseEntity<EnrollmentDTO> respondToEnrollment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        boolean approved = Boolean.TRUE.equals(body.get("approved"));
        String rejectionReason = (String) body.get("rejectionReason");
        return ResponseEntity.ok(enrollmentService.respondToEnrollment(id, approved, rejectionReason));
    }

    @PutMapping("/{id}/custom-plan")
    @Operation(summary = "Enregistrer le plan personnalisé (phases, séances) et notifier le stagiaire")
    public ResponseEntity<EnrollmentDTO> saveCustomPlan(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String customPlan = body.get("customPlan");
        String note = body.get("note");
        return ResponseEntity.ok(enrollmentService.saveCustomPlan(id, customPlan, note));
    }

    @DeleteMapping("/student/{studentId}/formation/{formationId}")
    @Operation(summary = "Désinscrire un stagiaire d'une formation")
    public ResponseEntity<Void> unenrollStudent(
            @PathVariable int studentId,
            @PathVariable Long formationId) {
        enrollmentService.unenrollStudent(studentId, formationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/formation/{formationId}")
    @Operation(summary = "Liste des inscriptions pour une formation")
    public ResponseEntity<List<EnrollmentDTO>> getEnrollmentsByFormation(@PathVariable Long formationId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByFormation(formationId));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Liste des inscriptions pour un stagiaire")
    public ResponseEntity<List<EnrollmentDTO>> getEnrollmentsByStudent(@PathVariable int studentId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByStudent(studentId));
    }

    @GetMapping("/formateur/{formateurId}/pending")
    @Operation(summary = "Demandes d'inscription en attente pour un formateur")
    public ResponseEntity<List<EnrollmentDTO>> getPendingForFormateur(@PathVariable int formateurId) {
        return ResponseEntity.ok(enrollmentService.getPendingEnrollmentsForFormateur(formateurId));
    }
}
