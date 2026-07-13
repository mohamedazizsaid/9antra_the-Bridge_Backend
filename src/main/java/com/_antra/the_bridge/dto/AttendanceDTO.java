package com._antra.the_bridge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDTO {
    private Long id;
    private Boolean present;
    private Integer starRating;   // 1-5, nullable
    private String sessionNote;   // optional formateur note
    private Integer studentId;
    private String studentFirstName;
    private String studentLastName;
    private String studentAvatar;
    private Long sessionId;
}

