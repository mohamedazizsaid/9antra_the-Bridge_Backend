package com._antra.the_bridge.repository;

import com._antra.the_bridge.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudentId(int studentId);

    List<Enrollment> findByFormationId(Long formationId);

    Optional<Enrollment> findByStudentIdAndFormationId(int studentId, Long formationId);

    boolean existsByStudentIdAndFormationId(int studentId, Long formationId);
}
