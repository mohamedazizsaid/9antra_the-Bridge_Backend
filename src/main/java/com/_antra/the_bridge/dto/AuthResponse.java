package com._antra.the_bridge.dto;

import com._antra.the_bridge.enumType.Role;
import com._antra.the_bridge.enumType.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String avatar;
    private int age;
    private Role role;
    private Status status;
    private LocalDate createdAt;
    private String authProvider;
    private String cin;
    private boolean onboardingCompleted;
}
