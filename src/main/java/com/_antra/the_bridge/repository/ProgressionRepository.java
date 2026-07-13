package com._antra.the_bridge.repository;

import com._antra.the_bridge.entity.Progression;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgressionRepository extends JpaRepository<Progression, Long> {

    List<Progression> findByStudentId(int studentId);

    List<Progression> findByPhaseId(Long phaseId);

    Optional<Progression> findByStudentIdAndPhaseId(int studentId, Long phaseId);
}
