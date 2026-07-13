package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.FormationDTO;
import com._antra.the_bridge.dto.PhaseDTO;
import com._antra.the_bridge.dto.SessionDTO;

import java.util.List;

public interface FormationService {
    List<FormationDTO> getAllFormations();
    FormationDTO getFormationById(Long id);
    List<FormationDTO> getFormationsByTrainer(int trainerId);
    List<FormationDTO> getFormationsByStudent(int studentId);
    FormationDTO getFormationDetailsForStudent(Long formationId, int studentId);

    // Creation & management
    FormationDTO createFormation(FormationDTO dto);
    PhaseDTO addPhaseToFormation(Long formationId, PhaseDTO dto);
    FormationDTO assignTrainers(Long formationId, List<Integer> trainerIds);
    SessionDTO addSessionToPhase(Long phaseId, SessionDTO dto);
    void closeSession(Long sessionId);
}

