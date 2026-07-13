package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.DTOHelper;
import com._antra.the_bridge.dto.EnrollmentDTO;
import com._antra.the_bridge.entity.Enrollment;
import com._antra.the_bridge.entity.Formation;
import com._antra.the_bridge.entity.User;
import com._antra.the_bridge.exception.CustomException;
import com._antra.the_bridge.repository.EnrollmentRepository;
import com._antra.the_bridge.repository.FormationRepository;
import com._antra.the_bridge.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final FormationRepository formationRepository;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository,
                                 UserRepository userRepository,
                                 FormationRepository formationRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.formationRepository = formationRepository;
    }

    @Override
    public EnrollmentDTO enrollStudent(int studentId, Long formationId) {
        if (enrollmentRepository.existsByStudentIdAndFormationId(studentId, formationId)) {
            throw new CustomException("Student already enrolled in this formation", HttpStatus.BAD_REQUEST);
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new CustomException("Student not found", HttpStatus.NOT_FOUND));
        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new CustomException("Formation not found", HttpStatus.NOT_FOUND));

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setFormation(formation);
        enrollment.setEnrollmentDate(LocalDate.now());

        enrollmentRepository.save(enrollment);
        return DTOHelper.toDTO(enrollment);
    }

    @Override
    public List<EnrollmentDTO> getEnrollmentsByFormation(Long formationId) {
        return enrollmentRepository.findByFormationId(formationId).stream()
                .map(DTOHelper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentDTO> getEnrollmentsByStudent(int studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(DTOHelper::toDTO)
                .collect(Collectors.toList());
    }
}
