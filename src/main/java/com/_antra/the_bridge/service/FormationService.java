package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.FormationDTO;

import java.util.List;

public interface FormationService {
    List<FormationDTO> getAllFormations();
    FormationDTO getFormationById(Long id);
    List<FormationDTO> getFormationsByTrainer(int trainerId);
    List<FormationDTO> getFormationsByStudent(int studentId);
    FormationDTO getFormationDetailsForStudent(Long formationId, int studentId);
}
