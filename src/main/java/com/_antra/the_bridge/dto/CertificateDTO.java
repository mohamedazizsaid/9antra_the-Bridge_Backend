package com._antra.the_bridge.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CertificateDTO {
    private Long id;
    private String certificateNumber;
    private String pdfUrl;
    private String hashValue;
    private String blockchainTransactionHash;
    private LocalDate issueDate;
    private Integer studentId;
    private String studentFirstName;
    private String studentLastName;
    private Long phaseId;
    private String phaseTitle;
    private Long formationId;
    private String formationTitle;
}
