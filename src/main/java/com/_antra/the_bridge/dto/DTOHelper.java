package com._antra.the_bridge.dto;

import com._antra.the_bridge.entity.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DTOHelper {

    public static UserDTO toDTO(User user) {
        if (user == null) return null;
        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .age(user.getAge())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .lastActivity(user.getLastActivity())
                .authProvider(user.getAuthProvider())
                .mustChangePassword(user.isMustChangePassword())
                .build();
    }

    public static FormationDTO toDTO(Formation formation) {
        if (formation == null) return null;
        List<Integer> studentIds = formation.getEnrollments() != null
                ? formation.getEnrollments().stream()
                    .filter(e -> e.getStudent() != null)
                    .map(e -> e.getStudent().getId())
                    .collect(Collectors.toList())
                : new ArrayList<>();
        return FormationDTO.builder()
                .id(formation.getId())
                .title(formation.getTitle())
                .description(formation.getDescription())
                .category(formation.getCategory())
                .totalPrice(formation.getTotalPrice())
                .status(formation.getStatus() != null ? formation.getStatus() : "PLANIFIEE")
                .archived(formation.isArchived())
                .startDate(formation.getStartDate())
                .endDate(formation.getEndDate())
                .defaultDurationWeeks(formation.getDefaultDurationWeeks())
                .phases(formation.getPhases() != null ?
                        formation.getPhases().stream().map(DTOHelper::toDTO).collect(Collectors.toList()) : null)
                .trainers(formation.getTrainers() != null ?
                        formation.getTrainers().stream().map(DTOHelper::toDTO).collect(Collectors.toList()) : null)
                .students(studentIds)
                .enrollmentCount(studentIds.size())
                .build();
    }

    public static PhaseDTO toDTO(Phase phase) {
        if (phase == null) return null;

        // Compute real attendance rate from closed sessions
        double attendanceRate = 0.0;
        if (phase.getSessions() != null) {
            List<Session> closedSessions = phase.getSessions().stream()
                    .filter(Session::isClosed)
                    .collect(Collectors.toList());
            if (!closedSessions.isEmpty()) {
                long totalSlots = 0;
                long totalPresent = 0;
                for (Session s : closedSessions) {
                    if (s.getAttendances() != null && !s.getAttendances().isEmpty()) {
                        totalSlots += s.getAttendances().size();
                        totalPresent += s.getAttendances().stream()
                                .filter(a -> Boolean.TRUE.equals(a.getPresent()))
                                .count();
                    }
                }
                if (totalSlots > 0) {
                    attendanceRate = (double) totalPresent / totalSlots * 100.0;
                }
            }
        }

        return PhaseDTO.builder()
                .id(phase.getId())
                .phaseOrder(phase.getPhaseOrder())
                .title(phase.getTitle())
                .content(phase.getContent())
                .price(phase.getPrice())
                .minimumAttendance(phase.getMinimumAttendance())
                .minimumGrade(phase.getMinimumGrade())
                .formationId(phase.getFormation() != null ? phase.getFormation().getId() : null)
                .unlocked(phase.getPhaseOrder() == 1)
                .attendanceRate(attendanceRate)
                .sessions(phase.getSessions() != null ?
                        phase.getSessions().stream().map(DTOHelper::toDTO).collect(Collectors.toList()) : null)
                .build();
    }

    public static SessionDTO toDTO(Session session) {
        if (session == null) return null;
        return SessionDTO.builder()
                .id(session.getId())
                .sessionDate(session.getSessionDate())
                .startTime(session.getStartTime())
                .duration(session.getDuration())
                .location(session.getLocation())
                .meetingLink(session.getMeetingLink())
                .phaseId(session.getPhase() != null ? session.getPhase().getId() : null)
                .phaseTitle(session.getPhase() != null ? session.getPhase().getTitle() : null)
                .formationId(session.getPhase() != null && session.getPhase().getFormation() != null ? 
                        session.getPhase().getFormation().getId() : null)
                .formationTitle(session.getPhase() != null && session.getPhase().getFormation() != null ? 
                        session.getPhase().getFormation().getTitle() : null)
                .closed(session.isClosed())
                .attendances(session.getAttendances() != null ?
                        session.getAttendances().stream().map(DTOHelper::toDTO).collect(Collectors.toList()) : null)
                .build();
    }

    public static AttendanceDTO toDTO(Attendance attendance) {
        if (attendance == null) return null;
        return AttendanceDTO.builder()
                .id(attendance.getId())
                .present(attendance.getPresent())
                .starRating(attendance.getStarRating())
                .sessionNote(attendance.getSessionNote())
                .studentId(attendance.getStudent() != null ? attendance.getStudent().getId() : 0)
                .studentFirstName(attendance.getStudent() != null ? attendance.getStudent().getFirstName() : null)
                .studentLastName(attendance.getStudent() != null ? attendance.getStudent().getLastName() : null)
                .studentAvatar(attendance.getStudent() != null ? attendance.getStudent().getAvatar() : null)
                .sessionId(attendance.getSession() != null ? attendance.getSession().getId() : null)
                .build();
    }

    public static EnrollmentDTO toDTO(Enrollment enrollment) {
        if (enrollment == null) return null;
        Formation formation = enrollment.getFormation();
        User firstTrainer = (formation != null && formation.getTrainers() != null && !formation.getTrainers().isEmpty())
                ? formation.getTrainers().get(0) : null;
        return EnrollmentDTO.builder()
                .id(enrollment.getId())
                .enrollmentDate(enrollment.getEnrollmentDate())
                .status(enrollment.getStatus() != null ? enrollment.getStatus().name() : "APPROVED")
                .customDurationWeeks(enrollment.getCustomDurationWeeks())
                .motivationMessage(enrollment.getMotivationMessage())
                .rejectionReason(enrollment.getRejectionReason())
                .respondedAt(enrollment.getRespondedAt())
                .studentId(enrollment.getStudent() != null ? enrollment.getStudent().getId() : 0)
                .studentFirstName(enrollment.getStudent() != null ? enrollment.getStudent().getFirstName() : null)
                .studentLastName(enrollment.getStudent() != null ? enrollment.getStudent().getLastName() : null)
                .studentEmail(enrollment.getStudent() != null ? enrollment.getStudent().getEmail() : null)
                .studentAvatar(enrollment.getStudent() != null ? enrollment.getStudent().getAvatar() : null)
                .formationId(formation != null ? formation.getId() : null)
                .formationTitle(formation != null ? formation.getTitle() : null)
                .formationDefaultDurationWeeks(formation != null ? formation.getDefaultDurationWeeks() : null)
                .formateurId(firstTrainer != null ? firstTrainer.getId() : null)
                .formateurName(firstTrainer != null ? firstTrainer.getFirstName() + " " + firstTrainer.getLastName() : null)
                .customPlan(enrollment.getCustomPlan())
                .build();
    }

    public static PaymentDTO toDTO(Payment payment) {
        if (payment == null) return null;
        return PaymentDTO.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionReference(payment.getTransactionReference())
                .receiptUrl(payment.getReceiptUrl())
                .enrollmentId(payment.getEnrollment() != null ? payment.getEnrollment().getId() : null)
                .phaseId(payment.getPhase() != null ? payment.getPhase().getId() : null)
                .phaseTitle(payment.getPhase() != null ? payment.getPhase().getTitle() : null)
                .phaseOrder(payment.getPhase() != null ? payment.getPhase().getPhaseOrder() : 0)
                .formationId(payment.getEnrollment() != null && payment.getEnrollment().getFormation() != null ?
                        payment.getEnrollment().getFormation().getId() : null)
                .formationTitle(payment.getEnrollment() != null && payment.getEnrollment().getFormation() != null ?
                        payment.getEnrollment().getFormation().getTitle() : null)
                .studentId(payment.getEnrollment() != null && payment.getEnrollment().getStudent() != null ?
                        payment.getEnrollment().getStudent().getId() : 0)
                .studentFirstName(payment.getEnrollment() != null && payment.getEnrollment().getStudent() != null ?
                        payment.getEnrollment().getStudent().getFirstName() : null)
                .studentLastName(payment.getEnrollment() != null && payment.getEnrollment().getStudent() != null ?
                        payment.getEnrollment().getStudent().getLastName() : null)
                .studentEmail(payment.getEnrollment() != null && payment.getEnrollment().getStudent() != null ?
                        payment.getEnrollment().getStudent().getEmail() : null)
                .studentAvatar(payment.getEnrollment() != null && payment.getEnrollment().getStudent() != null ?
                        payment.getEnrollment().getStudent().getAvatar() : null)
                .dueDate(payment.getDueDate())
                .build();
    }

    public static CertificateDTO toDTO(Certificate certificate) {
        if (certificate == null) return null;
        return CertificateDTO.builder()
                .id(certificate.getId())
                .certificateNumber(certificate.getCertificateNumber())
                .pdfUrl(certificate.getPdfUrl())
                .hashValue(certificate.getHashValue())
                .blockchainTransactionHash(certificate.getBlockchainTransactionHash())
                .issueDate(certificate.getIssueDate())
                .studentId(certificate.getStudent() != null ? certificate.getStudent().getId() : 0)
                .studentFirstName(certificate.getStudent() != null ? certificate.getStudent().getFirstName() : null)
                .studentLastName(certificate.getStudent() != null ? certificate.getStudent().getLastName() : null)
                .phaseId(certificate.getPhase() != null ? certificate.getPhase().getId() : null)
                .phaseTitle(certificate.getPhase() != null ? certificate.getPhase().getTitle() : null)
                .formationId(certificate.getPhase() != null && certificate.getPhase().getFormation() != null ?
                        certificate.getPhase().getFormation().getId() : null)
                .formationTitle(certificate.getPhase() != null && certificate.getPhase().getFormation() != null ?
                        certificate.getPhase().getFormation().getTitle() : null)
                .build();
    }

    public static NotificationDTO toDTO(Notification notification) {
        if (notification == null) return null;
        return NotificationDTO.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .readStatus(notification.isReadStatus())
                .createdAt(notification.getCreatedAt())
                .userId(notification.getUser() != null ? notification.getUser().getId() : 0)
                .build();
    }

    public static EvaluationDTO toDTO(Evaluation evaluation) {
        if (evaluation == null) return null;
        return EvaluationDTO.builder()
                .id(evaluation.getId())
                .grade(evaluation.getGrade())
                .comment(evaluation.getComment())
                .skills(evaluation.getSkills())
                .evaluationDate(evaluation.getEvaluationDate())
                .studentId(evaluation.getStudent() != null ? evaluation.getStudent().getId() : 0)
                .studentFirstName(evaluation.getStudent() != null ? evaluation.getStudent().getFirstName() : null)
                .studentLastName(evaluation.getStudent() != null ? evaluation.getStudent().getLastName() : null)
                .studentAvatar(evaluation.getStudent() != null ? evaluation.getStudent().getAvatar() : null)
                .trainerId(evaluation.getTrainer() != null ? evaluation.getTrainer().getId() : 0)
                .trainerFirstName(evaluation.getTrainer() != null ? evaluation.getTrainer().getFirstName() : null)
                .trainerLastName(evaluation.getTrainer() != null ? evaluation.getTrainer().getLastName() : null)
                .phaseId(evaluation.getPhase() != null ? evaluation.getPhase().getId() : null)
                .phaseTitle(evaluation.getPhase() != null ? evaluation.getPhase().getTitle() : null)
                .formationId(evaluation.getPhase() != null && evaluation.getPhase().getFormation() != null ?
                        evaluation.getPhase().getFormation().getId() : null)
                .formationTitle(evaluation.getPhase() != null && evaluation.getPhase().getFormation() != null ?
                        evaluation.getPhase().getFormation().getTitle() : null)
                .build();
    }

    public static ProgressionDTO toDTO(Progression progression) {
        if (progression == null) return null;
        return ProgressionDTO.builder()
                .id(progression.getId())
                .paymentValidated(progression.isPaymentValidated())
                .pedagogicalValidated(progression.isPedagogicalValidated())
                .unlocked(progression.isUnlocked())
                .validationDate(progression.getValidationDate())
                .studentId(progression.getStudent() != null ? progression.getStudent().getId() : 0)
                .phaseId(progression.getPhase() != null ? progression.getPhase().getId() : null)
                .phaseTitle(progression.getPhase() != null ? progression.getPhase().getTitle() : null)
                .build();
    }
}

