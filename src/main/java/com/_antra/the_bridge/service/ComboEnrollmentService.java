package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.ComboEnrollmentDTO;

import java.util.List;

/**
 * Service de gestion des inscriptions combo (parcours personnalisé).
 */
public interface ComboEnrollmentService {

    /**
     * Crée un nouveau ComboEnrollment en PENDING_PAYMENT et génère
     * la session Stripe Checkout correspondante.
     *
     * @param studentId    ID du stagiaire
     * @param formationIds IDs des formations choisies (min. 2)
     * @param note         Note optionnelle du stagiaire
     * @return DTO avec stripeCheckoutUrl pour redirection immédiate
     */
    ComboEnrollmentDTO createComboEnrollment(int studentId, List<Long> formationIds, String note);

    /**
     * Vérifie et confirme le paiement Stripe d'un combo.
     * Si "paid" : passe status → ACTIVE, génère les Enrollment individuels (APPROVED),
     * envoie un email de confirmation.
     *
     * @param stripeSessionId ID de la session Stripe
     * @param comboId         ID du ComboEnrollment
     * @return DTO mis à jour (status ACTIVE, enrollments créés)
     */
    ComboEnrollmentDTO verifyComboPayment(String stripeSessionId, Long comboId);

    /** Tous les combos d'un stagiaire */
    List<ComboEnrollmentDTO> getCombosByStudent(int studentId);

    /**
     * Combos impliquant les formations d'un formateur (vue formateur).
     * Inclut détail formations, phases, séances et stagiaires.
     */
    List<ComboEnrollmentDTO> getCombosByFormateur(int formateurId);

    /** Tous les combos — vue admin (supervision globale) */
    List<ComboEnrollmentDTO> getAllCombos();

    /** Détail d'un combo par son ID */
    ComboEnrollmentDTO getComboById(Long id);

    /**
     * Annule un combo en PENDING_PAYMENT (avant paiement).
     * Status → CANCELLED. Impossible d'annuler un combo ACTIVE.
     */
    void cancelCombo(Long comboId, int studentId);

    /**
     * Supprime définitivement un combo annulé ou non payé ainsi que ses inscriptions associées.
     */
    void deleteCombo(Long comboId, int studentId);

    /**
     * Régénère une session Stripe pour un combo existant en attente de paiement.
     */
    ComboEnrollmentDTO retryCheckout(Long comboId, int studentId);
}
