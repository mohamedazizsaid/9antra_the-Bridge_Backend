package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.EvaluationDTO;

import java.util.List;

public interface EvaluationService {
    List<EvaluationDTO> getEvaluationsByStudent(int studentId);
    EvaluationDTO saveEvaluation(EvaluationDTO evaluationDTO);
    List<EvaluationDTO> getEvaluationsByPhase(Long phaseId);
    List<EvaluationDTO> getEvaluationsByTrainer(int trainerId);
}
