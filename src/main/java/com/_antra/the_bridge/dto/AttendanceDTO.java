package com._antra.the_bridge.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceDTO {
    private Long id;
    private Boolean present;
    private int studentId;
    private String studentFirstName;
    private String studentLastName;
    private String studentAvatar;
    private Long sessionId;
}
