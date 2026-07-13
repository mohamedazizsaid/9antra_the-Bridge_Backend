package com._antra.the_bridge.controller;

import com._antra.the_bridge.dto.EnrollmentDTO;
import com._antra.the_bridge.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@Tag(name = "Inscriptions", description = "Endpoints pour gérer les inscriptions aux formations")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    @Operation(summary = "Inscrire un stagiaire à une formation")
    public ResponseEntity<EnrollmentDTO> enrollStudent(@RequestBody EnrollmentDTO enrollmentDTO) {
        return ResponseEntity.ok(enrollmentService.enrollStudent(
                enrollmentDTO.getStudentId(),
                enrollmentDTO.getFormationId()
        ));
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
}
