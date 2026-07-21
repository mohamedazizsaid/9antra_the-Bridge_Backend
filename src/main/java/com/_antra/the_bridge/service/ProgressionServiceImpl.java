package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.DTOHelper;
import com._antra.the_bridge.dto.NotificationDTO;
import com._antra.the_bridge.entity.*;
import com._antra.the_bridge.enumType.PaymentStatus;
import com._antra.the_bridge.exception.CustomException;
import com._antra.the_bridge.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProgressionServiceImpl implements ProgressionService {

    private final ProgressionRepository progressionRepository;
    private final UserRepository userRepository;
    private final PhaseRepository phaseRepository;
    private final PaymentRepository paymentRepository;
    private final EvaluationRepository evaluationRepository;
    private final AttendanceRepository attendanceRepository;
    private final SessionRepository sessionRepository;
    private final CertificateService certificateService;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MailService mailService;

    public ProgressionServiceImpl(ProgressionRepository progressionRepository,
                                  UserRepository userRepository,
                                  PhaseRepository phaseRepository,
                                  PaymentRepository paymentRepository,
                                  EvaluationRepository evaluationRepository,
                                  AttendanceRepository attendanceRepository,
                                  SessionRepository sessionRepository,
                                  CertificateService certificateService,
                                  NotificationRepository notificationRepository,
                                  SimpMessagingTemplate messagingTemplate,
                                  MailService mailService) {
        this.progressionRepository = progressionRepository;
        this.userRepository = userRepository;
        this.phaseRepository = phaseRepository;
        this.paymentRepository = paymentRepository;
        this.evaluationRepository = evaluationRepository;
        this.attendanceRepository = attendanceRepository;
        this.sessionRepository = sessionRepository;
        this.certificateService = certificateService;
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
        this.mailService = mailService;
    }

    @Override
    public void checkAndUpdateProgress(int studentId, Long phaseId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new CustomException("Student not found", HttpStatus.NOT_FOUND));
        Phase phase = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new CustomException("Phase not found", HttpStatus.NOT_FOUND));

        Progression progression = progressionRepository.findByStudentIdAndPhaseId(studentId, phaseId)
                .orElse(new Progression());

        if (progression.getId() == null) {
            progression.setStudent(student);
            progression.setPhase(phase);
            // Default first phase to unlocked
            progression.setUnlocked(phase.getPhaseOrder() == 1);
        }

        // 1. Check Payment Status
        List<Payment> payments = paymentRepository.findByStudentId(studentId);
        boolean isPaid = false;
        for (Payment payment : payments) {
            if (payment.getPhase() != null && payment.getPhase().getId().equals(phaseId)
                    && payment.getStatus() == PaymentStatus.COMPLETED) {
                isPaid = true;
                break;
            }
        }
        progression.setPaymentValidated(isPaid);

        // 2. Check Pedagogical Status (Grade & Attendance)
        Optional<Evaluation> evaluationOpt = evaluationRepository.findByStudentIdAndPhaseId(studentId, phaseId);
        boolean pedagogicalOk = false;
        
        if (evaluationOpt.isPresent()) {
            Evaluation evaluation = evaluationOpt.get();
            double grade = evaluation.getGrade() != null ? evaluation.getGrade() : 0.0;
            double minGrade = phase.getMinimumGrade() != null ? phase.getMinimumGrade() : 0.0;

            // Calculate attendance rate
            List<Session> phaseSessions = sessionRepository.findByPhaseId(phaseId);
            double attendanceRate = 100.0;
            if (!phaseSessions.isEmpty()) {
                long presentCount = 0;
                long evaluatedSessions = 0;
                for (Session session : phaseSessions) {
                    Optional<Attendance> attOpt = attendanceRepository.findByStudentIdAndSessionId(studentId, session.getId());
                    if (attOpt.isPresent()) {
                        evaluatedSessions++;
                        if (Boolean.TRUE.equals(attOpt.get().getPresent())) {
                            presentCount++;
                        }
                    }
                }
                attendanceRate = evaluatedSessions > 0 ? (double) presentCount / evaluatedSessions * 100 : 100.0;
            }

            double minAttendance = phase.getMinimumAttendance() != null ? phase.getMinimumAttendance() : 0.0;
            if (grade >= minGrade && attendanceRate >= minAttendance) {
                pedagogicalOk = true;
            }
        }
        progression.setPedagogicalValidated(pedagogicalOk);

        // 3. Mark Validated if both are true and issue certificate
        boolean alreadyValidated = progression.getValidationDate() != null;
        if (progression.isPaymentValidated() && progression.isPedagogicalValidated()) {
            if (!alreadyValidated) {
                progression.setValidationDate(LocalDate.now());
                
                // Issue blockchain certificate
                com._antra.the_bridge.dto.CertificateDTO cert = certificateService.generateCertificate(studentId, phaseId);

                // Send certificate email to student
                if (student.getEmail() != null && cert != null) {
                    String formationTitle = phase.getFormation() != null ? phase.getFormation().getTitle() : "N/A";
                    mailService.sendCertificateEmail(
                        student.getEmail(),
                        student.getFirstName(),
                        student.getLastName(),
                        phase.getTitle(),
                        formationTitle,
                        cert.getCertificateNumber(),
                        cert.getBlockchainTransactionHash(),
                        cert.getIssueDate() != null ? cert.getIssueDate().toString() : LocalDate.now().toString()
                    );
                }

                // Create and send notification
                createAndSendNotification(student, "Certificat disponible 🎓", 
                        "Félicitations ! Votre certificat pour la phase \"" + phase.getTitle() + "\" a été généré sur la blockchain Polygon.");

                // Unlock next phase if exists
                unlockNextPhase(student, phase);
            }
        } else {
            progression.setValidationDate(null);
        }

        progressionRepository.save(progression);
    }

    private void unlockNextPhase(User student, Phase currentPhase) {
        if (currentPhase.getFormation() == null) return;
        
        List<Phase> phases = phaseRepository.findByFormationIdOrderByPhaseOrder(currentPhase.getFormation().getId());
        Phase nextPhase = null;
        for (Phase p : phases) {
            if (p.getPhaseOrder() == currentPhase.getPhaseOrder() + 1) {
                nextPhase = p;
                break;
            }
        }

        if (nextPhase != null) {
            Progression nextProgression = progressionRepository.findByStudentIdAndPhaseId(student.getId(), nextPhase.getId())
                    .orElse(new Progression());
            
            if (nextProgression.getId() == null) {
                nextProgression.setStudent(student);
                nextProgression.setPhase(nextPhase);
            }
            nextProgression.setUnlocked(true);
            progressionRepository.save(nextProgression);

            createAndSendNotification(student, "Nouvelle phase débloquée 🚀", 
                    "La phase \"" + nextPhase.getTitle() + "\" est désormais accessible !");
        }
    }

    private void createAndSendNotification(User user, String title, String message) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setReadStatus(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUser(user);

        notificationRepository.save(notification);

        // STOMP WebSocket push notification
        try {
            NotificationDTO dto = DTOHelper.toDTO(notification);
            messagingTemplate.convertAndSend("/topic/notifications/" + user.getId(), dto);
        } catch (Exception e) {
            System.err.println("Failed to send STOMP websocket notification: " + e.getMessage());
        }
    }
}
