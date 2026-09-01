package com._antra.the_bridge.repository;

import com._antra.the_bridge.entity.ComboEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ComboEnrollmentRepository extends JpaRepository<ComboEnrollment, Long> {

    /** Tous les combos d'un stagiaire */
    List<ComboEnrollment> findByStudentId(int studentId);

    /** Combo par référence de reçu */
    Optional<ComboEnrollment> findByReceiptRef(String receiptRef);

    /** Combo par ID de session Stripe */
    Optional<ComboEnrollment> findByStripeSessionId(String stripeSessionId);

    /**
     * Combos impliquant les formations d'un formateur donné.
     * Utile pour la vue formateur.
     */
    @Query("SELECT DISTINCT ce FROM ComboEnrollment ce " +
           "JOIN ce.formations f " +
           "JOIN f.trainers t " +
           "WHERE t.id = :formateurId")
    List<ComboEnrollment> findByFormateur(@Param("formateurId") int formateurId);

    /**
     * Vérifie si une formation est déjà dans un combo actif (ACTIVE) d'un étudiant.
     * Utilisé pour griser les formations déjà souscrites en combo.
     */
    @Query("SELECT COUNT(ce) > 0 FROM ComboEnrollment ce " +
           "JOIN ce.formations f " +
           "WHERE ce.student.id = :studentId AND f.id = :formationId " +
           "AND ce.status IN ('PENDING_PAYMENT', 'ACTIVE')")
    boolean existsActiveComboForStudentAndFormation(
            @Param("studentId") int studentId,
            @Param("formationId") Long formationId);
}
