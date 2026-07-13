package com._antra.the_bridge.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class EvaluationDTO {
    private Long id;
    private Double grade;
    private String comment;
    private String skills;
    private LocalDate evaluationDate;
    private Integer studentId;
    private String studentFirstName;
    private String studentLastName;
    private String studentAvatar;
    private Integer trainerId;
    private String trainerFirstName;
    private String trainerLastName;
    private Long phaseId;
    private String phaseTitle;
    private Long formationId;
    private String formationTitle;
}
