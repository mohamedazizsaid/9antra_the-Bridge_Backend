package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.ProgressionDTO;
import java.util.List;

public interface ProgressionService {
    void checkAndUpdateProgress(int studentId, Long phaseId);
    List<ProgressionDTO> getProgressionsByStudent(int studentId);
}
