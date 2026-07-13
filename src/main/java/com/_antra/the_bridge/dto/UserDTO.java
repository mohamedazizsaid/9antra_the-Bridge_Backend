package com._antra.the_bridge.dto;

import com._antra.the_bridge.enumType.Role;
import com._antra.the_bridge.enumType.Status;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UserDTO {
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
    private LocalDate lastActivity;
    private String authProvider;
}
