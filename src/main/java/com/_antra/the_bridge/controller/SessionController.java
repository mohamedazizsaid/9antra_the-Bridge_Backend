package com._antra.the_bridge.controller;

import com._antra.the_bridge.dto.SessionDTO;
import com._antra.the_bridge.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@Tag(name = "Séances", description = "Endpoints pour gérer les séances de cours")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/today")
    @Operation(summary = "Séances programmées aujourd'hui")
    public ResponseEntity<List<SessionDTO>> getTodaySessions() {
        return ResponseEntity.ok(sessionService.getTodaySessions());
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Prochaines séances programmées")
    public ResponseEntity<List<SessionDTO>> getUpcomingSessions() {
        return ResponseEntity.ok(sessionService.getUpcomingSessions());
    }

    @GetMapping("/phase/{phaseId}")
    @Operation(summary = "Séances d'une phase spécifique")
    public ResponseEntity<List<SessionDTO>> getSessionsByPhase(@PathVariable Long phaseId) {
        return ResponseEntity.ok(sessionService.getSessionsByPhase(phaseId));
    }

    @PostMapping
    @Operation(summary = "Créer une nouvelle séance")
    public ResponseEntity<SessionDTO> createSession(@RequestBody SessionDTO sessionDTO) {
        return ResponseEntity.ok(sessionService.createSession(sessionDTO));
    }
}
