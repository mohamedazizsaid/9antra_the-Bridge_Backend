package com._antra.the_bridge.controller;

import com._antra.the_bridge.dto.AttendanceDTO;
import com._antra.the_bridge.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@Tag(name = "Présences", description = "Endpoints pour l'appel et le suivi de présence")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Feuille de présence pour une séance")
    public ResponseEntity<List<AttendanceDTO>> getAttendancesBySession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(attendanceService.getAttendancesBySession(sessionId));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Historique de présences d'un stagiaire")
    public ResponseEntity<List<AttendanceDTO>> getAttendancesByStudent(@PathVariable int studentId) {
        return ResponseEntity.ok(attendanceService.getAttendancesByStudent(studentId));
    }

    @PostMapping("/session/{sessionId}")
    @Operation(summary = "Enregistrer la feuille d'appel pour une séance")
    public ResponseEntity<Void> saveAttendanceList(
            @PathVariable Long sessionId,
            @RequestBody List<AttendanceDTO> attendanceDTOs) {
        attendanceService.saveAttendanceList(sessionId, attendanceDTOs);
        return ResponseEntity.ok().build();
    }
}
