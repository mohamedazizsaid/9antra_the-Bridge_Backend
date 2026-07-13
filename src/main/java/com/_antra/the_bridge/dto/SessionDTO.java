package com._antra.the_bridge.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class SessionDTO {
    private Long id;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private Integer duration;
    private String location;
    private String meetingLink;
    private Long phaseId;
    private String phaseTitle;
    private Long formationId;
    private String formationTitle;
    private List<AttendanceDTO> attendances;
}
