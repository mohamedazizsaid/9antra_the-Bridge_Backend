package com._antra.the_bridge.controller;

import com._antra.the_bridge.dto.EvaluationDTO;
import com._antra.the_bridge.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import com._antra.the_bridge.entity.User;
import com._antra.the_bridge.repository.UserRepository;
import java.security.Principal;
import java.util.Optional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluations")
@Tag(name = "Ã‰valuations", description = "Endpoints pour saisir et consulter les notes et compÃ©tences")
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final UserRepository userRepository;

    public EvaluationController(EvaluationService evaluationService, UserRepository userRepository) {
        this.evaluationService = evaluationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Ã‰valuations et notes reÃ§ues par un stagiaire")
    public ResponseEntity<List<EvaluationDTO>> getEvaluationsByStudent(@PathVariable int studentId) {
        return ResponseEntity.ok(evaluationService.getEvaluationsByStudent(studentId));
    }

    @GetMapping("/trainer/{trainerId}")
    @Operation(summary = "Historique des Ã©valuations donnÃ©es par un formateur")
    public ResponseEntity<List<EvaluationDTO>> getEvaluationsByTrainer(@PathVariable int trainerId) {
        return ResponseEntity.ok(evaluationService.getEvaluationsByTrainer(trainerId));
    }

    @PostMapping
    @Operation(summary = "Enregistrer ou modifier une Ã©valuation de phase")
    public ResponseEntity<EvaluationDTO> saveEvaluation(@RequestBody EvaluationDTO evaluationDTO) {
        return ResponseEntity.ok(evaluationService.saveEvaluation(evaluationDTO));
    }

    @GetMapping("/phase/{phaseId}")
    @Operation(summary = "Ã‰valuations des stagiaires pour une phase spÃ©cifique")
    public ResponseEntity<List<EvaluationDTO>> getEvaluationsByPhase(@PathVariable Long phaseId) {
        return ResponseEntity.ok(evaluationService.getEvaluationsByPhase(phaseId));
    }

    @GetMapping("/my")
    @Operation(summary = "Évaluations du stagiaire connecté")
    public ResponseEntity<List<EvaluationDTO>> getMyEvaluations(Principal principal) {
        Optional<User> userOpt = userRepository.findByEmail(principal.getName());
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(evaluationService.getEvaluationsByStudent(userOpt.get().getId()));
        }
        return ResponseEntity.notFound().build();
    }
}
