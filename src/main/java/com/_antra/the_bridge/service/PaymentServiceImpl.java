package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.DTOHelper;
import com._antra.the_bridge.dto.PaymentDTO;
import com._antra.the_bridge.entity.Enrollment;
import com._antra.the_bridge.entity.Payment;
import com._antra.the_bridge.entity.Phase;
import com._antra.the_bridge.enumType.PaymentStatus;
import com._antra.the_bridge.exception.CustomException;
import com._antra.the_bridge.repository.EnrollmentRepository;
import com._antra.the_bridge.repository.PaymentRepository;
import com._antra.the_bridge.repository.PhaseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PhaseRepository phaseRepository;
    private final ProgressionService progressionService;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              EnrollmentRepository enrollmentRepository,
                              PhaseRepository phaseRepository,
                              ProgressionService progressionService) {
        this.paymentRepository = paymentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.phaseRepository = phaseRepository;
        this.progressionService = progressionService;
    }

    @Override
    public List<PaymentDTO> getPaymentsByStudent(int studentId) {
        return paymentRepository.findByStudentId(studentId).stream()
                .map(DTOHelper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDTO> getPaymentsByFormation(Long formationId) {
        return paymentRepository.findByFormationId(formationId).stream()
                .map(DTOHelper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentDTO savePayment(PaymentDTO paymentDTO) {
        Enrollment enrollment = enrollmentRepository.findById(paymentDTO.getEnrollmentId())
                .orElseThrow(() -> new CustomException("Enrollment not found", HttpStatus.NOT_FOUND));
        Phase phase = phaseRepository.findById(paymentDTO.getPhaseId())
                .orElseThrow(() -> new CustomException("Phase not found", HttpStatus.NOT_FOUND));

        Payment payment = new Payment();
        payment.setAmount(paymentDTO.getAmount());
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMethod(paymentDTO.getPaymentMethod());
        payment.setStatus(PaymentStatus.COMPLETED); // Paid status by default when registering payment
        payment.setTransactionReference(paymentDTO.getTransactionReference());
        payment.setReceiptUrl(paymentDTO.getReceiptUrl());
        payment.setEnrollment(enrollment);
        payment.setPhase(phase);

        paymentRepository.save(payment);

        // Update student progress based on payment status validation
        if (enrollment.getStudent() != null) {
            progressionService.checkAndUpdateProgress(enrollment.getStudent().getId(), phase.getId());
        }

        return DTOHelper.toDTO(payment);
    }

    @Override
    public int getRetardCount(int studentId) {
        List<Payment> payments = paymentRepository.findByStudentId(studentId);
        int count = 0;
        for (Payment payment : payments) {
            if (payment.getStatus() == PaymentStatus.PENDING) {
                count++;
            }
        }
        return count;
    }
}
