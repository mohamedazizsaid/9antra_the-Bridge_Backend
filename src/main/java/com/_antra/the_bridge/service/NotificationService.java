package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.NotificationDTO;

import java.util.List;

public interface NotificationService {
    List<NotificationDTO> getNotificationsForUser(String email);
    void markAsRead(Long id);
    void markAllAsRead(String email);
}
