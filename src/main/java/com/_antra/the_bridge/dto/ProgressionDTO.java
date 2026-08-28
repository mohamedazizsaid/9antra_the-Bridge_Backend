package com._antra.the_bridge.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProgressionDTO {
    private Long id;
    private Integer studentId;
    private Long phaseId;
    private String formationTitle;
    private String phaseTitle;
    private Integer phaseOrder;
    private Boolean paymentValidated;
    private Boolean pedagogicalValidated;
    private Boolean unlocked;
    private LocalDate validationDate;
}
