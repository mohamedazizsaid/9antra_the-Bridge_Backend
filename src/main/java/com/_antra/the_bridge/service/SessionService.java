package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.SessionDTO;

import java.util.List;

public interface SessionService {
    List<SessionDTO> getTodaySessions();
    List<SessionDTO> getUpcomingSessions();
    List<SessionDTO> getSessionsByPhase(Long phaseId);
    List<SessionDTO> getTodaySessionsByTrainer(int trainerId);
    List<SessionDTO> getUpcomingSessionsByTrainer(int trainerId);
    List<SessionDTO> getPastSessionsByTrainer(int trainerId);
    List<SessionDTO> getAllSessionsByTrainer(int trainerId);
    SessionDTO createSession(SessionDTO sessionDTO);

}
