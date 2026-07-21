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
