package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.EnrollmentDTO;

import java.util.List;

public interface EnrollmentService {
    /**
     * Inscription avec les options standard (ancien endpoint, parcours par défaut, APPROVED immédiat).
     */
    EnrollmentDTO enrollStudent(int studentId, Long formationId);

    /**
     * Inscription avec choix du parcours :
     *  - customDurationWeeks == null → parcours par défaut (APPROVED immédiat)
     *  - customDurationWeeks != null → durée custom (PENDING, notification formateur)
     */
    EnrollmentDTO enrollStudentWithOptions(int studentId, Long formationId,
                                          Integer customDurationWeeks,
                                          String motivationMessage);

    /**
     * Réponse du formateur à une demande d'inscription custom.
     * Si approuvé, génère les paiements. Notifie le stagiaire dans les deux cas.
     */
    EnrollmentDTO respondToEnrollment(Long enrollmentId, boolean approved, String rejectionReason);

    void unenrollStudent(int studentId, Long formationId);

    List<EnrollmentDTO> getEnrollmentsByFormation(Long formationId);

    List<EnrollmentDTO> getEnrollmentsByStudent(int studentId);

    /** Récupère les demandes en attente (PENDING) pour un formateur donné. */
    List<EnrollmentDTO> getPendingEnrollmentsForFormateur(int formateurId);

    /** Sauvegarde le plan personnalisé (phases, séances) et notifie le stagiaire. */
    EnrollmentDTO saveCustomPlan(Long enrollmentId, String customPlan, String note);
}
