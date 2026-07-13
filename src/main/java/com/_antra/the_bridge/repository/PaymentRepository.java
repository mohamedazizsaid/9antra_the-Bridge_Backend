package com._antra.the_bridge.repository;

import com._antra.the_bridge.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p WHERE p.enrollment.student.id = :studentId")
    List<Payment> findByStudentId(@Param("studentId") int studentId);

    @Query("SELECT p FROM Payment p WHERE p.enrollment.formation.id = :formationId")
    List<Payment> findByFormationId(@Param("formationId") Long formationId);

    @Query("SELECT p FROM Payment p WHERE p.enrollment.id = :enrollmentId")
    List<Payment> findByEnrollmentId(@Param("enrollmentId") Long enrollmentId);
}
