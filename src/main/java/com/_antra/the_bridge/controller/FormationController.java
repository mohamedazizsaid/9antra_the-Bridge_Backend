package com._antra.the_bridge.controller;

import com._antra.the_bridge.dto.FormationDTO;
import com._antra.the_bridge.service.FormationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/formations")
@Tag(name = "Formations", description = "Endpoints pour la gestion des formations")
public class FormationController {

    private final FormationService formationService;

    public FormationController(FormationService formationService) {
        this.formationService = formationService;
    }

    @GetMapping
    @Operation(summary = "Liste de toutes les formations")
    public ResponseEntity<List<FormationDTO>> getAllFormations() {
        return ResponseEntity.ok(formationService.getAllFormations());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détails d'une formation par ID")
    public ResponseEntity<FormationDTO> getFormationById(@PathVariable Long id) {
        return ResponseEntity.ok(formationService.getFormationById(id));
    }

    @GetMapping("/formateur/{formateurId}")
    @Operation(summary = "Formations assignées à un formateur")
    public ResponseEntity<List<FormationDTO>> getFormationsByTrainer(@PathVariable int formateurId) {
        return ResponseEntity.ok(formationService.getFormationsByTrainer(formateurId));
    }

    @GetMapping("/stagiaire/{stagiaireId}")
    @Operation(summary = "Formations suivies par un stagiaire")
    public ResponseEntity<List<FormationDTO>> getFormationsByStudent(@PathVariable int stagiaireId) {
        return ResponseEntity.ok(formationService.getFormationsByStudent(stagiaireId));
    }

    @GetMapping("/{id}/student/{studentId}")
    @Operation(summary = "Progression et détails d'une formation pour un stagiaire spécifique")
    public ResponseEntity<FormationDTO> getFormationDetailsForStudent(
            @PathVariable Long id,
            @PathVariable int studentId) {
        return ResponseEntity.ok(formationService.getFormationDetailsForStudent(id, studentId));
    }
}
