package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.DTOHelper;
import com._antra.the_bridge.dto.EnrollmentDTO;
import com._antra.the_bridge.entity.*;
import com._antra.the_bridge.enumType.EnrollmentStatus;
import com._antra.the_bridge.enumType.PaymentStatus;
import com._antra.the_bridge.exception.CustomException;
import com._antra.the_bridge.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final FormationRepository formationRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository,
                                 UserRepository userRepository,
                                 FormationRepository formationRepository,
                                 PaymentRepository paymentRepository,
                                 NotificationRepository notificationRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.formationRepository = formationRepository;
        this.paymentRepository = paymentRepository;
        this.notificationRepository = notificationRepository;
    }

    // ─── Backward-compatible endpoint (parcours par défaut) ───────────────────
    @Override
    public EnrollmentDTO enrollStudent(int studentId, Long formationId) {
        return enrollStudentWithOptions(studentId, formationId, null, null);
    }

    // ─── Main enrollment method ────────────────────────────────────────────────
    @Override
    @Transactional
    public EnrollmentDTO enrollStudentWithOptions(int studentId, Long formationId,
                                                  Integer customDurationWeeks,
                                                  String motivationMessage) {
        if (enrollmentRepository.existsByStudentIdAndFormationId(studentId, formationId)) {
            throw new CustomException("Stagiaire déjà inscrit à cette formation", HttpStatus.BAD_REQUEST);
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new CustomException("Stagiaire introuvable", HttpStatus.NOT_FOUND));
        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new CustomException("Formation introuvable", HttpStatus.NOT_FOUND));

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setFormation(formation);
        enrollment.setEnrollmentDate(LocalDate.now());
        enrollment.setMotivationMessage(motivationMessage);

        if (customDurationWeeks != null) {
            // ── Durée personnalisée → PENDING ─────────────────────────────────
            enrollment.setCustomDurationWeeks(customDurationWeeks);
            enrollment.setStatus(EnrollmentStatus.PENDING);
            enrollmentRepository.save(enrollment);

            // Notifier chaque formateur de la formation
            if (formation.getTrainers() != null) {
                for (User trainer : formation.getTrainers()) {
                    sendNotification(trainer,
                            "📋 Nouvelle demande d'inscription",
                            String.format("Le stagiaire %s %s a demandé à s'inscrire à « %s » " +
                                            "avec une durée personnalisée de %d semaine(s). " +
                                            "Consultez les demandes en attente pour répondre.",
                                    student.getFirstName(), student.getLastName(),
                                    formation.getTitle(), customDurationWeeks));
                }
            }
        } else {
            // ── Parcours par défaut → APPROVED immédiat ───────────────────────
            enrollment.setStatus(EnrollmentStatus.APPROVED);
            enrollmentRepository.save(enrollment);
            generatePayments(enrollment, formation);

            // Notification de confirmation au stagiaire
            sendNotification(student,
                    "🎉 Inscription confirmée",
                    String.format("Votre inscription à la formation « %s » est confirmée ! " +
                            "Votre parcours standard a été activé.", formation.getTitle()));
        }

        return DTOHelper.toDTO(enrollment);
    }

    // ─── Réponse du formateur ──────────────────────────────────────────────────
    @Override
    @Transactional
    public EnrollmentDTO respondToEnrollment(Long enrollmentId, boolean approved, String rejectionReason) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new CustomException("Demande d'inscription introuvable", HttpStatus.NOT_FOUND));

        if (enrollment.getStatus() != EnrollmentStatus.PENDING) {
            throw new CustomException("Cette demande n'est plus en attente", HttpStatus.BAD_REQUEST);
        }

        enrollment.setRespondedAt(LocalDate.now());
        User student = enrollment.getStudent();
        Formation formation = enrollment.getFormation();

        if (approved) {
            enrollment.setStatus(EnrollmentStatus.APPROVED);
            enrollmentRepository.save(enrollment);
            generatePayments(enrollment, formation);

            sendNotification(student,
                    "✅ Demande approuvée",
                    String.format("Votre demande d'inscription à « %s » avec une durée de %d semaine(s) " +
                                    "a été approuvée par le formateur. Votre parcours est maintenant actif !",
                            formation.getTitle(), enrollment.getCustomDurationWeeks()));
        } else {
            enrollment.setStatus(EnrollmentStatus.REJECTED);
            enrollment.setRejectionReason(rejectionReason);
            enrollmentRepository.save(enrollment);

            String reason = (rejectionReason != null && !rejectionReason.isBlank())
                    ? " Motif : " + rejectionReason
                    : "";
            sendNotification(student,
                    "❌ Demande refusée",
                    String.format("Votre demande d'inscription à « %s » avec une durée de %d semaine(s) " +
                            "n'a pas été approuvée par le formateur.%s " +
                            "Vous pouvez vous inscrire avec le parcours standard.",
                            formation.getTitle(), enrollment.getCustomDurationWeeks(), reason));
        }

        return DTOHelper.toDTO(enrollment);
    }

    // ─── Queries ───────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void unenrollStudent(int studentId, Long formationId) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndFormationId(studentId, formationId)
                .orElseThrow(() -> new CustomException("Inscription introuvable", HttpStatus.NOT_FOUND));
        List<Payment> pendingPayments = paymentRepository.findByEnrollmentId(enrollment.getId())
                .stream().filter(p -> p.getStatus() == PaymentStatus.PENDING).collect(Collectors.toList());
        paymentRepository.deleteAll(pendingPayments);
        enrollmentRepository.delete(enrollment);
    }

    @Override
    public List<EnrollmentDTO> getEnrollmentsByFormation(Long formationId) {
        return enrollmentRepository.findByFormationId(formationId).stream()
                .map(DTOHelper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentDTO> getEnrollmentsByStudent(int studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(DTOHelper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentDTO> getPendingEnrollmentsForFormateur(int formateurId) {
        return enrollmentRepository.findPendingByFormateur(formateurId).stream()
                .map(DTOHelper::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EnrollmentDTO saveCustomPlan(Long enrollmentId, String customPlan, String note) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new CustomException("Inscription introuvable", HttpStatus.NOT_FOUND));

        enrollment.setCustomPlan(customPlan);
        Enrollment saved = enrollmentRepository.save(enrollment);

        // Notifier le stagiaire
        if (enrollment.getStudent() != null) {
            String formationTitle = enrollment.getFormation() != null ? enrollment.getFormation().getTitle() : "votre formation";
            String notifBody = "Votre formateur a configuré le planning personnalisé pour « " + formationTitle + " »"
                    + (enrollment.getCustomDurationWeeks() != null ? " (" + enrollment.getCustomDurationWeeks() + " semaines)" : "")
                    + (note != null && !note.isBlank() ? " : " + note : ".");
            sendNotification(enrollment.getStudent(), "📅 Planning personnalisé configuré", notifBody);
        }

        return DTOHelper.toDTO(saved);
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private void generatePayments(Enrollment enrollment, Formation formation) {
        if (formation.getPhases() != null && !formation.getPhases().isEmpty()) {
            for (Phase phase : formation.getPhases()) {
                Payment payment = new Payment();
                payment.setEnrollment(enrollment);
                payment.setPhase(phase);
                payment.setAmount(phase.getPrice() != null ? phase.getPrice() : 0.0);
                payment.setStatus(PaymentStatus.PENDING);
                int delay = (phase.getPhaseOrder() * 15) - 10;
                payment.setDueDate(LocalDate.now().plusDays(Math.max(1, delay)));
                paymentRepository.save(payment);
            }
        }
    }

    private void sendNotification(User recipient, String title, String message) {
        try {
            Notification notification = new Notification();
            notification.setUser(recipient);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setReadStatus(false);
            notification.setCreatedAt(LocalDateTime.now());
            notificationRepository.save(notification);
        } catch (Exception e) {
            // Ne pas bloquer le flux principal si la notification échoue
        }
    }
}
