package com._antra.the_bridge.dto;

import com._antra.the_bridge.enumType.Role;
import com._antra.the_bridge.enumType.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String avatar;
    private Integer age;
    private Role role;
    private Status status;
    private LocalDate createdAt;
    private LocalDate lastActivity;
    private String authProvider;
    private Boolean mustChangePassword;
    private String password; // Only used for formateur creation, never returned
    private String specialty;
}
