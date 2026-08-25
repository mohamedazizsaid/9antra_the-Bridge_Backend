package com._antra.the_bridge.dto;

import com._antra.the_bridge.enumType.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private Integer phaseOrder;
    private Long formationId;
    private String formationTitle;
    private Integer studentId;
    private String studentFirstName;
    private String studentLastName;
    private String studentEmail;
    private String studentAvatar;
    private LocalDate dueDate;
}
