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

    // Student info
    private Integer studentId;
    private String studentFirstName;
    private String studentLastName;
    private String studentEmail;
    private String studentAvatar;

    // Formation info
    private Long formationId;
    private String formationTitle;
    private Integer formationDefaultDurationWeeks;
    private Integer formateurId;
    private String formateurName;

    // Enrollment options
    private String status; // APPROVED | PENDING | REJECTED
    private Integer customDurationWeeks;
    private String motivationMessage;
    private String rejectionReason;
    private LocalDate respondedAt;
    private String customPlan;

    /** ID du ComboEnrollment associé (null si inscription individuelle) */
    private Long comboEnrollmentId;
}
