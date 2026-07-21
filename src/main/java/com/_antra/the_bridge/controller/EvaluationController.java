package com._antra.the_bridge.controller;

import com._antra.the_bridge.dto.EvaluationDTO;
import com._antra.the_bridge.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluations")
@Tag(name = "Évaluations", description = "Endpoints pour saisir et consulter les notes et compétences")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Évaluations et notes reçues par un stagiaire")
    public ResponseEntity<List<EvaluationDTO>> getEvaluationsByStudent(@PathVariable int studentId) {
        return ResponseEntity.ok(evaluationService.getEvaluationsByStudent(studentId));
    }

    @GetMapping("/trainer/{trainerId}")
    @Operation(summary = "Historique des évaluations données par un formateur")
    public ResponseEntity<List<EvaluationDTO>> getEvaluationsByTrainer(@PathVariable int trainerId) {
        return ResponseEntity.ok(evaluationService.getEvaluationsByTrainer(trainerId));
    }

    @PostMapping
    @Operation(summary = "Enregistrer ou modifier une évaluation de phase")
    public ResponseEntity<EvaluationDTO> saveEvaluation(@RequestBody EvaluationDTO evaluationDTO) {
        return ResponseEntity.ok(evaluationService.saveEvaluation(evaluationDTO));
    }

    @GetMapping("/phase/{phaseId}")
    @Operation(summary = "Évaluations des stagiaires pour une phase spécifique")
    public ResponseEntity<List<EvaluationDTO>> getEvaluationsByPhase(@PathVariable Long phaseId) {
        return ResponseEntity.ok(evaluationService.getEvaluationsByPhase(phaseId));
    }
}
