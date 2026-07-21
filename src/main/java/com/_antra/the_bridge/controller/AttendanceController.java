package com._antra.the_bridge.controller;

import com._antra.the_bridge.dto.AttendanceDTO;
import com._antra.the_bridge.service.AttendanceService;
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
@RequestMapping("/api/attendance")
@Tag(name = "PrÃ©sences", description = "Endpoints pour l'appel et le suivi de prÃ©sence")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final UserRepository userRepository;

    public AttendanceController(AttendanceService attendanceService, UserRepository userRepository) {
        this.attendanceService = attendanceService;
        this.userRepository = userRepository;
    }

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Feuille de prÃ©sence pour une sÃ©ance")
    public ResponseEntity<List<AttendanceDTO>> getAttendancesBySession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(attendanceService.getAttendancesBySession(sessionId));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Historique de prÃ©sences d'un stagiaire")
    public ResponseEntity<List<AttendanceDTO>> getAttendancesByStudent(@PathVariable int studentId) {
        return ResponseEntity.ok(attendanceService.getAttendancesByStudent(studentId));
    }

    @PostMapping("/session/{sessionId}")
    @Operation(summary = "Enregistrer la feuille d'appel pour une sÃ©ance")
    public ResponseEntity<Void> saveAttendanceList(
            @PathVariable Long sessionId,
            @RequestBody List<AttendanceDTO> attendanceDTOs) {
        attendanceService.saveAttendanceList(sessionId, attendanceDTOs);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my")
    @Operation(summary = "Présences du stagiaire connecté")
    public ResponseEntity<List<AttendanceDTO>> getMyAttendance(Principal principal) {
        Optional<User> userOpt = userRepository.findByEmail(principal.getName());
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(attendanceService.getAttendancesByStudent(userOpt.get().getId()));
        }
        return ResponseEntity.notFound().build();
    }
}
