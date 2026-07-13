package com._antra.the_bridge.dto;

import com._antra.the_bridge.enumType.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PaymentDTO {
    private Long id;
    private Double amount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private PaymentStatus status;
    private String transactionReference;
    private String receiptUrl;
    private Long enrollmentId;
    private Long phaseId;
    private String phaseTitle;
    private int phaseOrder;
    private Long formationId;
    private String formationTitle;
    private int studentId;
}
