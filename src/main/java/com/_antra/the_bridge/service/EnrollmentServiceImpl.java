package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.DTOHelper;
import com._antra.the_bridge.dto.EnrollmentDTO;
import com._antra.the_bridge.entity.*;
import com._antra.the_bridge.enumType.PaymentStatus;
import com._antra.the_bridge.exception.CustomException;
import com._antra.the_bridge.repository.EnrollmentRepository;
import com._antra.the_bridge.repository.FormationRepository;
import com._antra.the_bridge.repository.PaymentRepository;
import com._antra.the_bridge.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final FormationRepository formationRepository;
    private final PaymentRepository paymentRepository;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository,
                                 UserRepository userRepository,
                                 FormationRepository formationRepository,
                                 PaymentRepository paymentRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.formationRepository = formationRepository;
        this.paymentRepository = paymentRepository;
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
        if (formation.getPhases() != null && !formation.getPhases().isEmpty()) {
            for (Phase phase : formation.getPhases()) {
                Payment payment = new Payment();
                payment.setEnrollment(enrollment);
                payment.setPhase(phase);
                payment.setAmount(phase.getPrice() != null ? phase.getPrice() : 0.0);
                payment.setStatus(PaymentStatus.PENDING);
                int delay = (phase.getPhaseOrder() * 15) - 10;
                payment.setDueDate(LocalDate.now().plusDays(Math.max(1, delay)));
                paymentRepository.save(payment);
            }
        }
        return DTOHelper.toDTO(enrollment);
    }

    @Override
    @Transactional
    public void unenrollStudent(int studentId, Long formationId) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndFormationId(studentId, formationId)
                .orElseThrow(() -> new CustomException("Enrollment not found", HttpStatus.NOT_FOUND));
        List<Payment> pendingPayments = paymentRepository.findByEnrollmentId(enrollment.getId())
                .stream().filter(p -> p.getStatus() == PaymentStatus.PENDING).collect(Collectors.toList());
        paymentRepository.deleteAll(pendingPayments);
        enrollmentRepository.delete(enrollment);
    }

    @Override
    public List<EnrollmentDTO> getEnrollmentsByFormation(Long formationId) {
        return enrollmentRepository.findByFormationId(formationId).stream()
                .map(DTOHelper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentDTO> getEnrollmentsByStudent(int studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(DTOHelper::toDTO).collect(Collectors.toList());
    }
}
