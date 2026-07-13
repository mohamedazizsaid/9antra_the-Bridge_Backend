package com._antra.the_bridge.scheduler;

import com._antra.the_bridge.dto.DTOHelper;
import com._antra.the_bridge.dto.NotificationDTO;
import com._antra.the_bridge.entity.*;
import com._antra.the_bridge.enumType.PaymentStatus;
import com._antra.the_bridge.repository.NotificationRepository;
import com._antra.the_bridge.repository.PaymentRepository;
import com._antra.the_bridge.service.MailService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Scheduler checking payments daily to send reminders to students
 * 10, 7 and 5 days before the phase payment due date.
 */
@Component
public class PaymentReminderScheduler {

    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MailService mailService;

    public PaymentReminderScheduler(PaymentRepository paymentRepository,
                                    NotificationRepository notificationRepository,
                                    SimpMessagingTemplate messagingTemplate,
                                    MailService mailService) {
        this.paymentRepository = paymentRepository;
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
        this.mailService = mailService;
    }

    // Run every day at 8:30 AM AND every hour for rapid checks
    @Scheduled(cron = "0 30 8 * * ?")
    @Scheduled(fixedRate = 3600_000)
    public void sendPaymentReminders() {
        LocalDate today = LocalDate.now();
        List<Payment> allPayments = paymentRepository.findAll();

        for (Payment payment : allPayments) {
            if (payment.getStatus() != PaymentStatus.PENDING || payment.getDueDate() == null) {
                continue;
            }

            long daysRemaining = ChronoUnit.DAYS.between(today, payment.getDueDate());

            if (daysRemaining == 10 || daysRemaining == 7 || daysRemaining == 5) {
                Enrollment enrollment = payment.getEnrollment();
                if (enrollment == null) continue;
                User student = enrollment.getStudent();
                if (student == null) continue;
                Phase phase = payment.getPhase();
                String phaseTitle = phase != null ? phase.getTitle() : "Phase de formation";

                String reminderTitle = "⚠️ Rappel de paiement — Échéance dans " + daysRemaining + " jours";
                String reminderMsg = "Le paiement de " + payment.getAmount() + " TND pour la phase « " + phaseTitle 
                        + " » arrive à échéance le " + payment.getDueDate().toString() + ".";

                sendPaymentReminderTo(student, reminderTitle, reminderMsg, payment, (int) daysRemaining);
            }
        }
    }

    private void sendPaymentReminderTo(User student, String title, String message, Payment payment, int daysRemaining) {
        // Prevent sending duplicate notifications on the same day for this specific payment
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        boolean alreadySent = notificationRepository.existsByUserIdAndTitleAndCreatedAtAfter(
                student.getId(), title, startOfDay
        );
        if (alreadySent) return;

        // Persist notification
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setReadStatus(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUser(student);
        notificationRepository.save(notification);

        // Push real-time notification
        try {
            NotificationDTO dto = DTOHelper.toDTO(notification);
            messagingTemplate.convertAndSend("/topic/notifications/" + student.getId(), dto);
        } catch (Exception e) {
            System.err.println("STOMP payment reminder failed: " + e.getMessage());
        }

        // Send email reminder
        try {
            Phase phase = payment.getPhase();
            String phaseTitle = phase != null ? phase.getTitle() : "Phase de formation";
            mailService.sendPaymentReminder(
                    student.getEmail(),
                    student.getFirstName(),
                    phaseTitle,
                    payment.getAmount(),
                    payment.getDueDate(),
                    daysRemaining
            );
        } catch (Exception e) {
            System.err.println("Email payment reminder failed: " + e.getMessage());
        }
    }
}
