package com._antra.the_bridge.repository;

import com._antra.the_bridge.entity.Phase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhaseRepository extends JpaRepository<Phase, Long> {
    List<Phase> findByFormationIdOrderByPhaseOrder(Long formationId);
}
