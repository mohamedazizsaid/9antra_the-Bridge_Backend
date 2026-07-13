package com._antra.the_bridge.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ProgressionDTO {
    private Long id;
    private boolean paymentValidated;
    private boolean pedagogicalValidated;
    private boolean unlocked;
    private LocalDate validationDate;
    private Integer studentId;
    private Long phaseId;
    private String phaseTitle;
}
