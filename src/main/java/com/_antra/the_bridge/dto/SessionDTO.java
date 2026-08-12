package com._antra.the_bridge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private boolean closed;
    private List<AttendanceDTO> attendances;
}
