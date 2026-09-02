package com._antra.the_bridge.repository;

import com._antra.the_bridge.entity.StageInscription;
import com._antra.the_bridge.enumType.InternshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StageInscriptionRepository extends JpaRepository<StageInscription, Long> {

    Optional<StageInscription> findByStudentId(int studentId);

    List<StageInscription> findAllByStudentIdOrderByCreatedAtDesc(int studentId);

    List<StageInscription> findAllByStudentIdOrderByCreatedAtAsc(int studentId);

    List<StageInscription> findByStatus(InternshipStatus status);

    List<StageInscription> findByStudentEmail(String email);

    List<StageInscription> findBySupervisorId(int supervisorId);

    boolean existsByStudentId(int studentId);
}

