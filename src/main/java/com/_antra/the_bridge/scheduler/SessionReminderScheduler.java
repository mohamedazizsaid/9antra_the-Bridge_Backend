package com._antra.the_bridge.scheduler;

import com._antra.the_bridge.dto.DTOHelper;
import com._antra.the_bridge.dto.NotificationDTO;
import com._antra.the_bridge.entity.*;
import com._antra.the_bridge.repository.*;
import com._antra.the_bridge.service.MailService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Scheduled task that checks every minute if any session starts in the next 30 minutes
 * and sends real-time STOMP + e-mail reminders to enrolled students and the trainer.
 */
@Component
public class SessionReminderScheduler {

    private final SessionRepository sessionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MailService mailService;

    public SessionReminderScheduler(SessionRepository sessionRepository,
                                    EnrollmentRepository enrollmentRepository,
                                    NotificationRepository notificationRepository,
                                    SimpMessagingTemplate messagingTemplate,
                                    MailService mailService) {
        this.sessionRepository = sessionRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
        this.mailService = mailService;
    }

    @Scheduled(fixedRate = 60_000) // Every 60 seconds
    public void sendSessionReminders() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalTime in30 = now.plusMinutes(30);

        // Find all sessions today whose start time is between now and now+30min
        List<Session> todaySessions = sessionRepository.findBySessionDate(today);

        for (Session session : todaySessions) {
            if (session.getStartTime() == null) continue;
            LocalTime start = session.getStartTime();

            // Window: session starts in [now, now+30min]
            if (!start.isAfter(now) || !start.isBefore(in30)) continue;

            Phase phase = session.getPhase();
            if (phase == null) continue;
            Formation formation = phase.getFormation();
            if (formation == null) continue;

            String sessionTitle = formation.getTitle() + " — " + phase.getTitle();
            String reminderTitle = "Rappel de séance 📅";
            String reminderMsg = "La séance « " + sessionTitle + " » commence dans 30 minutes."
                    + " Lieu : " + (session.getLocation() != null ? session.getLocation() : (session.getMeetingLink() != null ? session.getMeetingLink() : "À définir"));

            // Notify enrolled students
            List<Enrollment> enrollments = enrollmentRepository.findByFormationId(formation.getId());
            for (Enrollment enrollment : enrollments) {
                User student = enrollment.getStudent();
                sendReminderTo(student, reminderTitle, reminderMsg, session);
            }

            // Notify trainers
            for (User trainer : formation.getTrainers()) {
                String trainerMsg = "Vous animez la séance « " + sessionTitle + " » dans 30 minutes."
                        + " Lieu : " + (session.getLocation() != null ? session.getLocation() : (session.getMeetingLink() != null ? session.getMeetingLink() : "À définir"));
                sendReminderTo(trainer, "Rappel formateur 👨‍🏫", trainerMsg, session);
            }
        }
    }

    private void sendReminderTo(User user, String title, String message, Session session) {
        // Avoid duplicate notifications: skip if already sent within the last 35 minutes
        boolean alreadySent = notificationRepository.existsByUserIdAndTitleAndCreatedAtAfter(
                user.getId(), title,
                LocalDateTime.now().minusMinutes(35)
        );
        if (alreadySent) return;

        // Persist in-app notification
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setReadStatus(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUser(user);
        notificationRepository.save(notification);

        // Push via STOMP WebSocket
        try {
            NotificationDTO dto = DTOHelper.toDTO(notification);
            messagingTemplate.convertAndSend("/topic/notifications/" + user.getId(), dto);
        } catch (Exception e) {
            System.err.println("STOMP push failed for user " + user.getId() + ": " + e.getMessage());
        }

        // Send email reminder
        try {
            mailService.sendSessionReminder(user.getEmail(), user.getFirstName(), message, session);
        } catch (Exception e) {
            System.err.println("Email reminder failed for " + user.getEmail() + ": " + e.getMessage());
        }
    }
}
