package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.AttendanceDTO;
import com._antra.the_bridge.dto.DTOHelper;
import com._antra.the_bridge.entity.Attendance;
import com._antra.the_bridge.entity.Session;
import com._antra.the_bridge.entity.User;
import com._antra.the_bridge.exception.CustomException;
import com._antra.the_bridge.repository.AttendanceRepository;
import com._antra.the_bridge.repository.SessionRepository;
import com._antra.the_bridge.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository,
                                 SessionRepository sessionRepository,
                                 UserRepository userRepository) {
        this.attendanceRepository = attendanceRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<AttendanceDTO> getAttendancesBySession(Long sessionId) {
        return attendanceRepository.findBySessionId(sessionId).stream()
                .map(DTOHelper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDTO> getAttendancesByStudent(int studentId) {
        return attendanceRepository.findByStudentId(studentId).stream()
                .map(DTOHelper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void saveAttendanceList(Long sessionId, List<AttendanceDTO> attendanceDTOs) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException("Session not found", HttpStatus.NOT_FOUND));

        for (AttendanceDTO dto : attendanceDTOs) {
            Optional<Attendance> attOpt = attendanceRepository.findByStudentIdAndSessionId(dto.getStudentId(), sessionId);
            Attendance attendance;
            if (attOpt.isPresent()) {
                attendance = attOpt.get();
            } else {
                attendance = new Attendance();
                User student = userRepository.findById(dto.getStudentId())
                        .orElseThrow(() -> new CustomException("Student not found: " + dto.getStudentId(), HttpStatus.NOT_FOUND));
                attendance.setStudent(student);
                attendance.setSession(session);
            }
            attendance.setPresent(dto.getPresent());
            // Persist star rating and note if provided
            if (dto.getStarRating() != null) {
                attendance.setStarRating(dto.getStarRating());
            }
            if (dto.getSessionNote() != null) {
                attendance.setSessionNote(dto.getSessionNote());
            }
            attendanceRepository.save(attendance);
        }
    }
}

