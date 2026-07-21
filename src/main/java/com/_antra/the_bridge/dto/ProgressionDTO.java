package com._antra.the_bridge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressionDTO {
    private Long id;
    private Integer studentId;
    private Long phaseId;
    private String formationTitle;
    private String phaseTitle;
    private Integer phaseOrder;
    private boolean paymentValidated;
    private boolean pedagogicalValidated;
    private boolean unlocked;
    private LocalDate validationDate;
}
