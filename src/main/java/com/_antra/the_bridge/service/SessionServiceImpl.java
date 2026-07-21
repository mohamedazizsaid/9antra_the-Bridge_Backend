package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.DTOHelper;
import com._antra.the_bridge.dto.SessionDTO;
import com._antra.the_bridge.entity.Phase;
import com._antra.the_bridge.entity.Session;
import com._antra.the_bridge.exception.CustomException;
import com._antra.the_bridge.repository.PhaseRepository;
import com._antra.the_bridge.repository.SessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final PhaseRepository phaseRepository;

    public SessionServiceImpl(SessionRepository sessionRepository, PhaseRepository phaseRepository) {
        this.sessionRepository = sessionRepository;
        this.phaseRepository = phaseRepository;
    }

    @Override
    public List<SessionDTO> getTodaySessions() {
        return sessionRepository.findToday(LocalDate.now()).stream()
                .map(DTOHelper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SessionDTO> getUpcomingSessions() {
        return sessionRepository.findUpcoming(LocalDate.now()).stream()
                .map(DTOHelper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SessionDTO> getSessionsByPhase(Long phaseId) {
        return sessionRepository.findByPhaseId(phaseId).stream()
                .map(DTOHelper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SessionDTO> getTodaySessionsByTrainer(int trainerId) {
        return sessionRepository.findTodayByTrainerId(trainerId, LocalDate.now()).stream()
                .map(DTOHelper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SessionDTO> getUpcomingSessionsByTrainer(int trainerId) {
        return sessionRepository.findUpcomingByTrainerId(trainerId, LocalDate.now()).stream()
                .map(DTOHelper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SessionDTO createSession(SessionDTO sessionDTO) {
        Phase phase = phaseRepository.findById(sessionDTO.getPhaseId())
                .orElseThrow(() -> new CustomException("Phase not found", HttpStatus.NOT_FOUND));

        Session session = new Session();
        session.setSessionDate(sessionDTO.getSessionDate());
        session.setStartTime(sessionDTO.getStartTime());
        session.setDuration(sessionDTO.getDuration());
        session.setLocation(sessionDTO.getLocation());
        session.setMeetingLink(sessionDTO.getMeetingLink());
        session.setPhase(phase);

        sessionRepository.save(session);
        return DTOHelper.toDTO(session);
    }
}
