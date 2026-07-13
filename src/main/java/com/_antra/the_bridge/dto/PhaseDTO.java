package com._antra.the_bridge.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PhaseDTO {
    private Long id;
    private int phaseOrder;
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
