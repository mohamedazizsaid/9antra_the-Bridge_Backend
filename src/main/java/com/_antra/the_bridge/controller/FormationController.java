package com._antra.the_bridge.controller;

import com._antra.the_bridge.dto.FormationDTO;
import com._antra.the_bridge.dto.PhaseDTO;
import com._antra.the_bridge.dto.SessionDTO;
import com._antra.the_bridge.service.FormationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Formations", description = "Endpoints pour la gestion des formations")
public class FormationController {

    private final FormationService formationService;

    public FormationController(FormationService formationService) {
        this.formationService = formationService;
    }

    @GetMapping("/api/formations")
    @Operation(summary = "Liste de toutes les formations")
    public ResponseEntity<List<FormationDTO>> getAllFormations() {
        return ResponseEntity.ok(formationService.getAllFormations());
    }

    @GetMapping("/api/formations/{id}")
    @Operation(summary = "Détails d'une formation par ID")
    public ResponseEntity<FormationDTO> getFormationById(@PathVariable Long id) {
        return ResponseEntity.ok(formationService.getFormationById(id));
    }

    @GetMapping("/api/formations/formateur/{formateurId}")
    @Operation(summary = "Formations assignées à un formateur")
    public ResponseEntity<List<FormationDTO>> getFormationsByTrainer(@PathVariable int formateurId) {
        return ResponseEntity.ok(formationService.getFormationsByTrainer(formateurId));
    }

    @GetMapping("/api/formations/stagiaire/{stagiaireId}")
    @Operation(summary = "Formations suivies par un stagiaire")
    public ResponseEntity<List<FormationDTO>> getFormationsByStudent(@PathVariable int stagiaireId) {
        return ResponseEntity.ok(formationService.getFormationsByStudent(stagiaireId));
    }

    @GetMapping("/api/formations/{id}/student/{studentId}")
    @Operation(summary = "Progression et détails d'une formation pour un stagiaire spécifique")
    public ResponseEntity<FormationDTO> getFormationDetailsForStudent(
            @PathVariable Long id,
            @PathVariable int studentId) {
        return ResponseEntity.ok(formationService.getFormationDetailsForStudent(id, studentId));
    }

    @GetMapping("/api/stats/dashboard")
    @Operation(summary = "Statistiques globales du dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(formationService.getDashboardStats());
    }

    // ─── Creation & Management ────────────────────────────────────────────────

    @PostMapping("/api/formations")
    @Operation(summary = "Créer une nouvelle formation (avec phases et sessions optionnelles)")
    public ResponseEntity<FormationDTO> createFormation(@RequestBody FormationDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(formationService.createFormation(dto));
    }

    @PutMapping("/api/formations/{id}")
    @Operation(summary = "Modifier une formation existante (titre, description, catégorie, prix, status)")
    public ResponseEntity<FormationDTO> updateFormation(@PathVariable Long id, @RequestBody FormationDTO dto) {
        return ResponseEntity.ok(formationService.updateFormation(id, dto));
    }

    @PatchMapping("/api/formations/{id}/archive")
    @Operation(summary = "Archiver ou désarchiver une formation (toggle)")
    public ResponseEntity<Void> archiveFormation(@PathVariable Long id) {
        formationService.archiveFormation(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/formations/{id}")
    @Operation(summary = "Supprimer une formation (admin ou formateur propriétaire)")
    public ResponseEntity<Void> deleteFormation(@PathVariable Long id) {
        formationService.deleteFormation(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/formations/{formationId}/phases")
    @Operation(summary = "Ajouter une phase à une formation existante")
    public ResponseEntity<PhaseDTO> addPhaseToFormation(
            @PathVariable Long formationId,
            @RequestBody PhaseDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(formationService.addPhaseToFormation(formationId, dto));
    }

    @PutMapping("/api/formations/{formationId}/trainers")
    @Operation(summary = "Affecter des formateurs à une formation (liste d'IDs)")
    public ResponseEntity<FormationDTO> assignTrainers(
            @PathVariable Long formationId,
            @RequestBody List<Integer> trainerIds) {
        return ResponseEntity.ok(formationService.assignTrainers(formationId, trainerIds));
    }

    @PostMapping("/api/phases/{phaseId}/sessions")
    @Operation(summary = "Ajouter une session à une phase")
    public ResponseEntity<SessionDTO> addSessionToPhase(
            @PathVariable Long phaseId,
            @RequestBody SessionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(formationService.addSessionToPhase(phaseId, dto));
    }

    @PostMapping("/api/sessions/{sessionId}/close")
    @Operation(summary = "Clôturer une séance — déclenche le check certificat si dernière séance")
    public ResponseEntity<Void> closeSession(@PathVariable Long sessionId) {
        formationService.closeSession(sessionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/phases/{phaseId}/unlock")
    @Operation(summary = "Débloquer une phase par un formateur/admin")
    public ResponseEntity<Void> unlockPhase(@PathVariable Long phaseId) {
        formationService.unlockPhase(phaseId);
        return ResponseEntity.ok().build();
    }
}
