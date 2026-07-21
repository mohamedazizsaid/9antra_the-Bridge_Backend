package com._antra.the_bridge.controller;

import com._antra.the_bridge.dto.UserDTO;
import com._antra.the_bridge.entity.AuditLog;
import com._antra.the_bridge.entity.Notification;
import com._antra.the_bridge.entity.User;
import com._antra.the_bridge.enumType.Role;
import com._antra.the_bridge.enumType.Status;
import com._antra.the_bridge.repository.AuditLogRepository;
import com._antra.the_bridge.repository.NotificationRepository;
import com._antra.the_bridge.repository.UserRepository;
import com._antra.the_bridge.service.MailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administration", description = "Endpoints d'administration de la plateforme")
public class AdminController {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public AdminController(UserRepository userRepository,
                           AuditLogRepository auditLogRepository,
                           NotificationRepository notificationRepository,
                           PasswordEncoder passwordEncoder,
                           MailService mailService) {
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    // ─── User Management ─────────────────────────────────────────────────────

    @GetMapping("/users/{id}")
    @Operation(summary = "Détails complet d'un utilisateur")
    public ResponseEntity<UserDTO> getUserById(@PathVariable int id) {
        return userRepository.findById(id)
                .map(u -> UserDTO.builder()
                        .id(u.getId())
                        .firstName(u.getFirstName())
                        .lastName(u.getLastName())
                        .email(u.getEmail())
                        .phone(u.getPhone())
                        .avatar(u.getAvatar())
                        .age(u.getAge())
                        .role(u.getRole())
                        .status(u.getStatus())
                        .createdAt(u.getCreatedAt())
                        .lastActivity(u.getLastActivity())
                        .authProvider(u.getAuthProvider())
                        .mustChangePassword(u.isMustChangePassword())
                        .build())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}/status")
    @Operation(summary = "Modifier le statut d'un utilisateur")
    public ResponseEntity<Void> updateUserStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
        userRepository.findById(id).ifPresent(u -> {
            try {
                u.setStatus(Status.valueOf(body.get("status")));
                userRepository.save(u);
            } catch (Exception ignored) {}
        });
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Supprimer un utilisateur")
    public ResponseEntity<Void> deleteUser(@PathVariable int id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        }
        return ResponseEntity.ok().build();
    }

    // ─── Create Formateur ─────────────────────────────────────────────────────

    @PostMapping("/formateurs")
    @Operation(summary = "Créer un compte formateur et envoyer les identifiants par email")
    public ResponseEntity<UserDTO> createFormateur(@RequestBody UserDTO dto) {
        // Generate a temp password
        String tempPassword = "Bridge@" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        User formateur = new User();
        formateur.setFirstName(dto.getFirstName());
        formateur.setLastName(dto.getLastName());
        formateur.setEmail(dto.getEmail());
        formateur.setPhone(dto.getPhone());
        formateur.setAge(dto.getAge() != null ? dto.getAge() : 30);
        formateur.setRole(Role.FORMATEUR);
        formateur.setStatus(Status.ACTIVE);
        formateur.setPassword(passwordEncoder.encode(tempPassword));
        formateur.setEmailVerified(true);
        formateur.setAuthProvider("LOCAL");
        formateur.setMustChangePassword(true);
        formateur.setCreatedAt(LocalDate.now());
        formateur.setAvatar("https://api.dicebear.com/7.x/initials/svg?seed=" +
                dto.getFirstName().charAt(0) + dto.getLastName().charAt(0) + "&backgroundColor=c62761");

        User saved = userRepository.save(formateur);

        // Send welcome email with credentials
        mailService.sendFormateurWelcomeEmail(dto.getEmail(), dto.getFirstName(), dto.getLastName(), tempPassword);

        return ResponseEntity.ok(UserDTO.builder()
                .id(saved.getId())
                .firstName(saved.getFirstName())
                .lastName(saved.getLastName())
                .email(saved.getEmail())
                .role(saved.getRole())
                .status(saved.getStatus())
                .mustChangePassword(true)
                .build());
    }

    // ─── Audit Logs ───────────────────────────────────────────────────────────

    @GetMapping("/logs")
    @Operation(summary = "Liste des logs de requêtes avec filtres avancés")
    public ResponseEntity<Map<String, Object>> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        Page<AuditLog> logsPage = auditLogRepository.findFiltered(
                method, ip, from, to,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));

        List<Map<String, Object>> logs = logsPage.getContent().stream().map(l -> Map.<String, Object>of(
                "id", l.getId(),
                "action", l.getAction() != null ? l.getAction() : "",
                "description", l.getDescription() != null ? l.getDescription() : "",
                "ipAddress", l.getIpAddress() != null ? l.getIpAddress() : "",
                "createdAt", l.getCreatedAt() != null ? l.getCreatedAt().toString() : "",
                "userName", l.getUser() != null ? l.getUser().getFirstName() + " " + l.getUser().getLastName() : "Anonyme"
        )).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "logs", logs,
                "totalElements", logsPage.getTotalElements(),
                "totalPages", logsPage.getTotalPages(),
                "currentPage", page
        ));
    }

    // ─── Statistics ───────────────────────────────────────────────────────────

    @GetMapping("/stats/extended")
    @Operation(summary = "Statistiques étendues de la plateforme")
    public ResponseEntity<Map<String, Object>> getExtendedStats() {
        long totalUsers = userRepository.count();
        long stagiaires = userRepository.findAll().stream().filter(u -> u.getRole() == Role.STAGIAIRE).count();
        long formateurs = userRepository.findAll().stream().filter(u -> u.getRole() == Role.FORMATEUR).count();
        long activeUsers = userRepository.findAll().stream().filter(u -> u.getStatus() == Status.ACTIVE).count();
        long totalLogs = auditLogRepository.count();

        // Users per month (last 6 months)
        List<User> allUsers = userRepository.findAll();
        Map<String, Long> usersByMonth = allUsers.stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(LocalDate.now().minusMonths(6)))
                .collect(Collectors.groupingBy(u -> u.getCreatedAt().getMonth().name(), Collectors.counting()));

        return ResponseEntity.ok(Map.of(
                "totalUsers", totalUsers,
                "stagiaires", stagiaires,
                "formateurs", formateurs,
                "activeUsers", activeUsers,
                "totalLogs", totalLogs,
                "usersByMonth", usersByMonth
        ));
    }

    // ─── Notification Broadcast ───────────────────────────────────────────────

    @PostMapping("/notifications/broadcast")
    @Operation(summary = "Diffuser une notification à des rôles sélectionnés")
    public ResponseEntity<Map<String, Object>> broadcastNotification(@RequestBody Map<String, Object> body) {
        String title = (String) body.get("title");
        String message = (String) body.get("message");
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) body.get("roles");

        if (title == null || message == null || roles == null || roles.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "title, message and roles are required"));
        }

        List<Role> targetRoles = roles.stream().map(r -> {
            try { return Role.valueOf(r); } catch (Exception e) { return null; }
        }).filter(r -> r != null).collect(Collectors.toList());

        List<User> targets = userRepository.findAll().stream()
                .filter(u -> targetRoles.contains(u.getRole()) && u.getStatus() == Status.ACTIVE)
                .collect(Collectors.toList());

        List<Notification> notifications = targets.stream().map(u -> {
            Notification n = new Notification();
            n.setUser(u);
            n.setTitle(title);
            n.setMessage(message);
            n.setReadStatus(false);
            n.setCreatedAt(LocalDateTime.now());
            return n;
        }).collect(Collectors.toList());

        notificationRepository.saveAll(notifications);

        return ResponseEntity.ok(Map.of(
                "sent", notifications.size(),
                "roles", roles
        ));
    }
}

