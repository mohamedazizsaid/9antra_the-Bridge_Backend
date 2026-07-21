package com._antra.the_bridge.controller;

import com._antra.the_bridge.dto.ProgressionDTO;
import com._antra.the_bridge.entity.User;
import com._antra.the_bridge.repository.UserRepository;
import com._antra.the_bridge.service.ProgressionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/progressions")
@Tag(name = "Progression", description = "Endpoints pour consulter les progressions")
public class ProgressionController {

    private final ProgressionService progressionService;
    private final UserRepository userRepository;

    public ProgressionController(ProgressionService progressionService, UserRepository userRepository) {
        this.progressionService = progressionService;
        this.userRepository = userRepository;
    }

    @GetMapping("/my")
    @Operation(summary = "Récupérer la progression du stagiaire connecté")
    public ResponseEntity<List<ProgressionDTO>> getMyProgressions(Principal principal) {
        Optional<User> userOpt = userRepository.findByEmail(principal.getName());
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(progressionService.getProgressionsByStudent(userOpt.get().getId()));
        }
        return ResponseEntity.notFound().build();
    }
}
