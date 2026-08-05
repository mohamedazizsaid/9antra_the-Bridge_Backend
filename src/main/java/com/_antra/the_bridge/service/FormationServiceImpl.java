package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.DTOHelper;
import com._antra.the_bridge.dto.FormationDTO;
import com._antra.the_bridge.dto.PhaseDTO;
import com._antra.the_bridge.dto.SessionDTO;
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
    private final PhaseRepository phaseRepository;
    private final SessionRepository sessionRepository;
    private final ProgressionService progressionService;
    private final EnrollmentRepository enrollmentRepository;

    public FormationServiceImpl(FormationRepository formationRepository,
                                UserRepository userRepository,
                                ProgressionRepository progressionRepository,
                                AttendanceRepository attendanceRepository,
                                EvaluationRepository evaluationRepository,
                                PhaseRepository phaseRepository,
                                SessionRepository sessionRepository,
                                ProgressionService progressionService,
                                EnrollmentRepository enrollmentRepository) {
        this.formationRepository = formationRepository;
        this.userRepository = userRepository;
        this.progressionRepository = progressionRepository;
        this.attendanceRepository = attendanceRepository;
        this.evaluationRepository = evaluationRepository;
        this.phaseRepository = phaseRepository;
        this.sessionRepository = sessionRepository;
        this.progressionService = progressionService;
        this.enrollmentRepository = enrollmentRepository;
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

            Optional<Progression> progOpt = progressionRepository.findByStudentIdAndPhaseId(studentId, phase.getId());
            if (progOpt.isPresent()) {
                Progression prog = progOpt.get();
                pDto.setPaymentValidated(prog.isPaymentValidated());
                pDto.setPedagogicalValidated(prog.isPedagogicalValidated());
                pDto.setUnlocked(prog.isUnlocked());
            } else {
                pDto.setPaymentValidated(false);
                pDto.setPedagogicalValidated(false);
                pDto.setUnlocked(phase.getPhaseOrder() == 1);
            }

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

    // ─── Creation & Management ────────────────────────────────────────────────

    @Override
    public FormationDTO createFormation(FormationDTO dto) {
        Formation formation = new Formation();
        formation.setTitle(dto.getTitle());
        formation.setDescription(dto.getDescription());
        formation.setCategory(dto.getCategory());
        formation.setTotalPrice(dto.getTotalPrice());

        // Assign trainers if provided
        if (dto.getTrainers() != null) {
            List<User> trainers = new ArrayList<>();
            for (var trainerDTO : dto.getTrainers()) {
                userRepository.findById(trainerDTO.getId()).ifPresent(trainers::add);
            }
            formation.setTrainers(trainers);
        }

        Formation saved = formationRepository.save(formation);

        // Create phases if provided
        if (dto.getPhases() != null) {
            for (PhaseDTO phaseDTO : dto.getPhases()) {
                Phase phase = new Phase();
                phase.setPhaseOrder(phaseDTO.getPhaseOrder());
                phase.setTitle(phaseDTO.getTitle());
                phase.setContent(phaseDTO.getContent());
                phase.setPrice(phaseDTO.getPrice());
                phase.setMinimumAttendance(phaseDTO.getMinimumAttendance() != null ? phaseDTO.getMinimumAttendance() : 75.0);
                phase.setMinimumGrade(phaseDTO.getMinimumGrade() != null ? phaseDTO.getMinimumGrade() : 10.0);
                phase.setFormation(saved);
                Phase savedPhase = phaseRepository.save(phase);

                // Create sessions per phase
                if (phaseDTO.getSessions() != null) {
                    for (SessionDTO sessionDTO : phaseDTO.getSessions()) {
                        Session session = new Session();
                        session.setSessionDate(sessionDTO.getSessionDate());
                        session.setStartTime(sessionDTO.getStartTime());
                        session.setDuration(sessionDTO.getDuration());
                        session.setLocation(sessionDTO.getLocation());
                        session.setMeetingLink(sessionDTO.getMeetingLink());
                        session.setPhase(savedPhase);
                        sessionRepository.save(session);
                    }
                }
            }
        }

        return DTOHelper.toDTO(formationRepository.findById(saved.getId()).orElse(saved));
    }

    @Override
    public PhaseDTO addPhaseToFormation(Long formationId, PhaseDTO dto) {
        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new CustomException("Formation not found", HttpStatus.NOT_FOUND));

        Phase phase = new Phase();
        phase.setPhaseOrder(dto.getPhaseOrder() > 0 ? dto.getPhaseOrder() : formation.getPhases().size() + 1);
        phase.setTitle(dto.getTitle());
        phase.setContent(dto.getContent());
        phase.setPrice(dto.getPrice());
        phase.setMinimumAttendance(dto.getMinimumAttendance() != null ? dto.getMinimumAttendance() : 75.0);
        phase.setMinimumGrade(dto.getMinimumGrade() != null ? dto.getMinimumGrade() : 10.0);
        phase.setFormation(formation);

        return DTOHelper.toDTO(phaseRepository.save(phase));
    }

    @Override
    public FormationDTO assignTrainers(Long formationId, List<Integer> trainerIds) {
        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new CustomException("Formation not found", HttpStatus.NOT_FOUND));

        List<User> trainers = new ArrayList<>();
        for (Integer tid : trainerIds) {
            userRepository.findById(tid).ifPresent(trainers::add);
        }
        formation.setTrainers(trainers);
        return DTOHelper.toDTO(formationRepository.save(formation));
    }

    @Override
    public SessionDTO addSessionToPhase(Long phaseId, SessionDTO dto) {
        Phase phase = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new CustomException("Phase not found", HttpStatus.NOT_FOUND));

        Session session = new Session();
        session.setSessionDate(dto.getSessionDate());
        session.setStartTime(dto.getStartTime());
        session.setDuration(dto.getDuration());
        session.setLocation(dto.getLocation());
        session.setMeetingLink(dto.getMeetingLink());
        session.setPhase(phase);

        return DTOHelper.toDTO(sessionRepository.save(session));
    }

    @Override
    public void closeSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException("Session not found", HttpStatus.NOT_FOUND));

        Phase phase = session.getPhase();
        if (phase == null) return;

        Formation formation = phase.getFormation();
        if (formation == null) return;

        // Trigger progress update & certificate check for all enrolled students
        List<Enrollment> enrollments = enrollmentRepository.findByFormationId(formation.getId());
        for (Enrollment enrollment : enrollments) {
            int studentId = enrollment.getStudent().getId();
            progressionService.checkAndUpdateProgress(studentId, phase.getId());
        }
    }
}

