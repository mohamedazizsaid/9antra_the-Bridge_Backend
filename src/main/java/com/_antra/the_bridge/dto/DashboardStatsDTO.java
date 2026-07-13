package com._antra.the_bridge.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsDTO {
    private long totalUsers;
    private long totalFormations;
    private long totalStagiaires;
    private long totalFormateurs;
    private long totalEnrollments;
    private long totalCertificates;
    private long unreadNotifications;
}
