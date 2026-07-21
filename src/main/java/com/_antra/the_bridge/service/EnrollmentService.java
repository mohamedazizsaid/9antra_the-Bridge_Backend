package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.EnrollmentDTO;

import java.util.List;

public interface EnrollmentService {
    EnrollmentDTO enrollStudent(int studentId, Long formationId);
    void unenrollStudent(int studentId, Long formationId);
    List<EnrollmentDTO> getEnrollmentsByFormation(Long formationId);
    List<EnrollmentDTO> getEnrollmentsByStudent(int studentId);
}
