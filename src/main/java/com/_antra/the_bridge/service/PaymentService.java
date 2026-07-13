package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.PaymentDTO;

import java.util.List;

public interface PaymentService {
    List<PaymentDTO> getPaymentsByStudent(int studentId);
    List<PaymentDTO> getPaymentsByFormation(Long formationId);
    PaymentDTO savePayment(PaymentDTO paymentDTO);
    int getRetardCount(int studentId);
}
