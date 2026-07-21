package com._antra.the_bridge.entity;

import com._antra.the_bridge.enumType.Role;
import com._antra.the_bridge.enumType.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String firstName;
    private String lastName;
    private String avatar;
    private int age;
    private String email;
    private String password;
    private String phone;

    private LocalDate createdAt;
    private LocalDate lastActivity;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private Status status;

    // Email verification
    private String verificationCode;
    private boolean emailVerified = false;
    private boolean mustChangePassword = false;

    // OAuth2 provider (LOCAL, GOOGLE, FACEBOOK)
    private String authProvider = "LOCAL";
    // OAuth2 provider user ID
    private String providerId;

    @OneToMany(mappedBy = "student")
    private List<Enrollment> enrollments;

    @OneToMany(mappedBy = "student")
    private List<Attendance> attendances;

    @OneToMany(mappedBy = "student")
    private List<Evaluation> evaluations;

    @OneToMany(mappedBy = "trainer")
    private List<Evaluation> givenEvaluations;

    @OneToMany(mappedBy = "student")
    private List<Progression> progressions;

    @OneToMany(mappedBy = "student")
    private List<Certificate> certificates;

    @OneToMany(mappedBy = "user")
    private List<Notification> notifications;

    @OneToMany(mappedBy = "user")
    private List<AuditLog> auditLogs;

    @ManyToMany(mappedBy = "trainers")
    private List<Formation> assignedFormations;


    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDate.now();
        }
        if (this.lastActivity == null) {
            this.lastActivity = LocalDate.now();
        }
    }
}


