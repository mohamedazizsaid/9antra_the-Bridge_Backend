package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.DashboardStatsDTO;
import com._antra.the_bridge.dto.UserDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();
    UserDTO getProfile(String email);
    UserDTO updateProfile(String email, UserDTO profileDTO);
    void changePassword(String email, String currentPassword, String newPassword);
    DashboardStatsDTO getAdminStats(String email);
}
