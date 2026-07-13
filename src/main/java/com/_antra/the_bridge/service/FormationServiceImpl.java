package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.DTOHelper;
import com._antra.the_bridge.dto.FormationDTO;
import com._antra.the_bridge.dto.PhaseDTO;
import com._antra.the_bridge.entity.*;
import com._antra.the_bridge.exception.CustomException;
import com._antra.the_bridge.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FormationServiceImpl implements FormationService {

    private final FormationRepository formationRepository;
    private final UserRepository userRepository;
    private final ProgressionRepository progressionRepository;
    private final AttendanceRepository attendanceRepository;
    private final EvaluationRepository evaluationRepository;

    public FormationServiceImpl(FormationRepository formationRepository,
                                UserRepository userRepository,
                                ProgressionRepository progressionRepository,
                                AttendanceRepository attendanceRepository,
                                EvaluationRepository evaluationRepository) {
        this.formationRepository = formationRepository;
        this.userRepository = userRepository;
        this.progressionRepository = progressionRepository;
        this.attendanceRepository = attendanceRepository;
        this.evaluationRepository = evaluationRepository;
    }

    @Override
    public List<FormationDTO> getAllFormations() {
        return formationRepository.findAll().stream()
                .map(DTOHelper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public FormationDTO getFormationById(Long id) {
        Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new CustomException("Formation not found", HttpStatus.NOT_FOUND));
        return DTOHelper.toDTO(formation);
    }

    @Override
    public List<FormationDTO> getFormationsByTrainer(int trainerId) {
        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> new CustomException("Trainer not found", HttpStatus.NOT_FOUND));
        return formationRepository.findByTrainer(trainer).stream()
                .map(DTOHelper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FormationDTO> getFormationsByStudent(int studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new CustomException("Student not found", HttpStatus.NOT_FOUND));
        return formationRepository.findByStudent(student).stream()
                .map(f -> getFormationDetailsForStudent(f.getId(), studentId))
                .collect(Collectors.toList());
    }

    @Override
    public FormationDTO getFormationDetailsForStudent(Long formationId, int studentId) {
        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new CustomException("Formation not found", HttpStatus.NOT_FOUND));

        List<PhaseDTO> phaseDTOs = new ArrayList<>();
        for (Phase phase : formation.getPhases()) {
            PhaseDTO pDto = DTOHelper.toDTO(phase);

            // Fetch progression for this student
            Optional<Progression> progOpt = progressionRepository.findByStudentIdAndPhaseId(studentId, phase.getId());
            if (progOpt.isPresent()) {
                Progression prog = progOpt.get();
                pDto.setPaymentValidated(prog.isPaymentValidated());
                pDto.setPedagogicalValidated(prog.isPedagogicalValidated());
                pDto.setUnlocked(prog.isUnlocked());
            } else {
                pDto.setPaymentValidated(false);
                pDto.setPedagogicalValidated(false);
                // Unlocked by default if it's the first phase
                pDto.setUnlocked(phase.getPhaseOrder() == 1);
            }

            // Calculate attendance rate for this student in this phase
            List<Session> sessions = phase.getSessions();
            if (sessions != null && !sessions.isEmpty()) {
                long attended = 0;
                long totalSessionsWithAttendance = 0;
                for (Session session : sessions) {
                    Optional<Attendance> attOpt = attendanceRepository.findByStudentIdAndSessionId(studentId, session.getId());
                    if (attOpt.isPresent()) {
                        totalSessionsWithAttendance++;
                        if (Boolean.TRUE.equals(attOpt.get().getPresent())) {
                            attended++;
                        }
                    }
                }
                double rate = totalSessionsWithAttendance > 0 ? (double) attended / totalSessionsWithAttendance * 100 : 100.0;
                pDto.setAttendanceRate(rate);
            } else {
                pDto.setAttendanceRate(100.0);
            }

            // Fetch evaluation grade
            Optional<Evaluation> evalOpt = evaluationRepository.findByStudentIdAndPhaseId(studentId, phase.getId());
            if (evalOpt.isPresent()) {
                pDto.setGrade(evalOpt.get().getGrade());
            }

            phaseDTOs.add(pDto);
        }

        FormationDTO dto = DTOHelper.toDTO(formation);
        dto.setPhases(phaseDTOs);
        return dto;
    }
}
