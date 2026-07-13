package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.SessionDTO;

import java.util.List;

public interface SessionService {
    List<SessionDTO> getTodaySessions();
    List<SessionDTO> getUpcomingSessions();
    List<SessionDTO> getSessionsByPhase(Long phaseId);
    SessionDTO createSession(SessionDTO sessionDTO);
}
