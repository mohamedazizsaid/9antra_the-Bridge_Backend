package com._antra.the_bridge.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class EnrollmentDTO {
    private Long id;
    private LocalDate enrollmentDate;
    private Integer studentId;
    private String studentFirstName;
    private String studentLastName;
    private String studentEmail;
    private String studentAvatar;
    private Long formationId;
    private String formationTitle;
}
