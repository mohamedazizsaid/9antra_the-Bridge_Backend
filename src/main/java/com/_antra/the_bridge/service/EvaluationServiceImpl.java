package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.DTOHelper;
import com._antra.the_bridge.dto.EvaluationDTO;
import com._antra.the_bridge.entity.Evaluation;
import com._antra.the_bridge.entity.Phase;
import com._antra.the_bridge.entity.User;
import com._antra.the_bridge.exception.CustomException;
import com._antra.the_bridge.repository.EvaluationRepository;
import com._antra.the_bridge.repository.PhaseRepository;
import com._antra.the_bridge.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EvaluationServiceImpl implements EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final UserRepository userRepository;
    private final PhaseRepository phaseRepository;
    private final ProgressionService progressionService;

    public EvaluationServiceImpl(EvaluationRepository evaluationRepository,
                                 UserRepository userRepository,
                                 PhaseRepository phaseRepository,
                                 ProgressionService progressionService) {
        this.evaluationRepository = evaluationRepository;
        this.userRepository = userRepository;
        this.phaseRepository = phaseRepository;
        this.progressionService = progressionService;
    }

    @Override
    public List<EvaluationDTO> getEvaluationsByStudent(int studentId) {
        return evaluationRepository.findByStudentId(studentId).stream()
                .map(DTOHelper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EvaluationDTO saveEvaluation(EvaluationDTO evaluationDTO) {
        User student = userRepository.findById(evaluationDTO.getStudentId())
                .orElseThrow(() -> new CustomException("Student not found", HttpStatus.NOT_FOUND));
        User trainer = userRepository.findById(evaluationDTO.getTrainerId())
                .orElseThrow(() -> new CustomException("Trainer not found", HttpStatus.NOT_FOUND));
        Phase phase = phaseRepository.findById(evaluationDTO.getPhaseId())
                .orElseThrow(() -> new CustomException("Phase not found", HttpStatus.NOT_FOUND));

        Evaluation evaluation = evaluationRepository.findByStudentIdAndPhaseId(evaluationDTO.getStudentId(), evaluationDTO.getPhaseId())
                .orElse(new Evaluation());

        evaluation.setGrade(evaluationDTO.getGrade());
        evaluation.setComment(evaluationDTO.getComment());
        evaluation.setSkills(evaluationDTO.getSkills());
        evaluation.setEvaluationDate(LocalDate.now());
        evaluation.setStudent(student);
        evaluation.setTrainer(trainer);
        evaluation.setPhase(phase);

        evaluationRepository.save(evaluation);

        // Update student progress based on new grade/evaluation
        progressionService.checkAndUpdateProgress(student.getId(), phase.getId());

        return DTOHelper.toDTO(evaluation);
    }

    @Override
    public List<EvaluationDTO> getEvaluationsByPhase(Long phaseId) {
        return evaluationRepository.findByPhaseId(phaseId).stream()
                .map(DTOHelper::toDTO)
                .collect(Collectors.toList());
    }
}
