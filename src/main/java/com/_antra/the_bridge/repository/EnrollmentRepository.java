package com._antra.the_bridge.repository;

import com._antra.the_bridge.entity.Enrollment;
import com._antra.the_bridge.enumType.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudentId(int studentId);

    List<Enrollment> findByFormationId(Long formationId);

    Optional<Enrollment> findByStudentIdAndFormationId(int studentId, Long formationId);

    boolean existsByStudentIdAndFormationId(int studentId, Long formationId);

    List<Enrollment> findByStudentIdAndStatus(int studentId, EnrollmentStatus status);

    /** Récupère toutes les demandes PENDING pour les formations dont ce formateur est trainer */
    @Query("SELECT e FROM Enrollment e " +
           "JOIN e.formation f " +
           "JOIN f.trainers t " +
           "WHERE t.id = :formateurId AND e.status = 'PENDING'")
    List<Enrollment> findPendingByFormateur(@Param("formateurId") int formateurId);
}
