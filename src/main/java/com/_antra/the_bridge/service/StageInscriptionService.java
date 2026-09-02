package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.OnboardingRequest;
import com._antra.the_bridge.dto.StageInscriptionDTO;
import com._antra.the_bridge.enumType.InternshipStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StageInscriptionService {

    /**
     * Soumet l'onboarding d'un stagiaire (étapes 1 à 7).
     * Upload les PDFs sur Cloudinary si wantsInternship=true.
     */
    StageInscriptionDTO submitOnboarding(int studentId, OnboardingRequest request,
                                         MultipartFile demande, MultipartFile lettre);

    /**
     * Récupère l'inscription du stagiaire connecté.
     */
    StageInscriptionDTO getMyInscription(int studentId);

    /**
     * [ADMIN] Récupère toutes les inscriptions.
     */
    List<StageInscriptionDTO> getAllInscriptions();

    /**
     * [ADMIN] Récupère une inscription par ID.
     */
    StageInscriptionDTO getInscriptionById(Long id);

    /**
     * [ADMIN] Met à jour le statut d'une inscription (APPROVED, REJECTED, etc.) et assigne éventuellement un formateur encadrant.
     */
    StageInscriptionDTO updateInscriptionStatus(Long id, InternshipStatus status, String notes, Integer supervisorId);

    /**
     * [ADMIN] Confirme un paiement manuel (MAIN_A_MAIN ou BANQUE).
     */
    StageInscriptionDTO confirmAdminPayment(Long id);

    /**
     * [ADMIN] Modifie l'état de paiement (payé / non payé).
     */
    StageInscriptionDTO updatePaymentStatus(Long id, boolean paid);

    /**
     * [FORMATEUR] Récupère les inscriptions de stages facultatifs assignées à ce formateur.
     */
    List<StageInscriptionDTO> getInscriptionsBySupervisor(int supervisorId);

    /**
     * [ADMIN] Clôture un stage (COMPLETED), génère le PDF d'attestation professionnelle
     * et retourne le DTO mis à jour avec l'URL du PDF.
     */
    StageInscriptionDTO cloturerStage(Long id);

    /**
     * Récupère l'historique complet de toutes les inscriptions d'un stagiaire.
     */
    List<StageInscriptionDTO> getMyInscriptionHistory(int studentId);

    /**
     * Génère ou rafraîchit une session de paiement Stripe pour une inscription de stage approuvée.
     */
    StageInscriptionDTO createStripeCheckoutSession(Long id);

    /**
     * Vérifie et confirme le paiement Stripe pour une inscription de stage facultatif.
     */
    StageInscriptionDTO verifyStripePayment(String sessionId, Long stageInscriptionId);
}
