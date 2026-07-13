package com._antra.the_bridge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhaseDTO {
    private Long id;
    private Integer phaseOrder;
    private String title;
    private String content;
    private Double price;
    private Double minimumAttendance;
    private Double minimumGrade;
    private Long formationId;
    private List<SessionDTO> sessions;
    // Progression info for current student (optional)
    private Boolean paymentValidated;
    private Boolean pedagogicalValidated;
    private Boolean unlocked;
    private Double attendanceRate;
    private Double grade;
}
