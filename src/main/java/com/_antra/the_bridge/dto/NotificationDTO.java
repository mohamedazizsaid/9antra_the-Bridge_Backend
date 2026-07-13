package com._antra.the_bridge.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationDTO {
    private Long id;
    private String title;
    private String message;
    private boolean readStatus;
    private LocalDateTime createdAt;
    private int userId;
}
