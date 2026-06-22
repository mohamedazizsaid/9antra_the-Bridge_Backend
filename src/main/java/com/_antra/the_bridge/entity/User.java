package com._antra.the_bridge.entity;

import com._antra.the_bridge.enumType.Role;
import com._antra.the_bridge.enumType.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    // OAuth2 provider (LOCAL, GOOGLE, FACEBOOK)
    private String authProvider = "LOCAL";
    // OAuth2 provider user ID
    private String providerId;

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

