package com._antra.the_bridge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionDTO {
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate sessionDate;
    @JsonFormat(pattern = "HH:mm[:ss]")
    private LocalTime startTime;
    private Integer duration;
    private String location;
    private String meetingLink;
    private Long phaseId;
    private String phaseTitle;
    private Long formationId;
    private String formationTitle;
    private Boolean closed;
    private List<AttendanceDTO> attendances;
}
