package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.AttendanceDTO;

import java.util.List;

public interface AttendanceService {
    List<AttendanceDTO> getAttendancesBySession(Long sessionId);
    List<AttendanceDTO> getAttendancesByStudent(int studentId);
    void saveAttendanceList(Long sessionId, List<AttendanceDTO> attendanceDTOs);
}
