package com._antra.the_bridge.entity;

import com._antra.the_bridge.enumType.InternshipPaymentMode;
import com._antra.the_bridge.enumType.InternshipStatus;
import com._antra.the_bridge.enumType.ReferralStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stage_inscriptions")
public class StageInscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─── Type d'inscription ────────────────────────────────────────────────
    /** true = stage facultatif, false = formations uniquement */
    private boolean wantsInternship = false;

    // ─── Infos stage (obligatoires si wantsInternship = true) ─────────────
    private String stageProjectTitle;
    private Integer stageDurationWeeks;

    /** URL Cloudinary du PDF "Demande de stage" */
    @Column(length = 1000)
    private String demandeStageUrl;

    /** URL Cloudinary du PDF "Lettre d'affectation" */
    @Column(length = 1000)
    private String lettreAffectationUrl;

    // ─── Formations sélectionnées ──────────────────────────────────────────
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "stage_inscription_formations",
        joinColumns = @JoinColumn(name = "stage_inscription_id"),
        inverseJoinColumns = @JoinColumn(name = "formation_id")
    )
    private List<Formation> selectedFormations = new ArrayList<>();

    // ─── Calcul des prix ───────────────────────────────────────────────────
    private Double originalPrice;
    private Double discountAmount;
    private String discountReason;
    private Double totalPrice;

    // ─── Parrainage ────────────────────────────────────────────────────────
    /** Email de la personne parrainée (filleul) */
    private String referralEmail;

    @Enumerated(EnumType.STRING)
    private ReferralStatus referralStatus;

    /** true si la remise parrainage (10%) a été appliquée au parrain */
    private boolean referralDiscountApplied = false;

    // ─── Mode de paiement ──────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    private InternshipPaymentMode paymentMode;

    /** true = payer maintenant, false = payer après */
    private boolean payNow = false;

    /** true si la remise paiement comptant (10%) a été appliquée */
    private boolean cashDiscountApplied = false;

    /** ID de session Stripe si paiement en ligne */
    private String stripeSessionId;

    /** URL checkout Stripe */
    @Column(length = 1000)
    private String stripePaymentUrl;

    /** true si le paiement via Stripe a été confirmé */
    private boolean stripePaymentConfirmed = false;

    // ─── Supervision Admin ─────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InternshipStatus status = InternshipStatus.PENDING_REVIEW;

    @Column(length = 2000)
    private String adminNotes;

    /** Paiement manuel (main-à-main / banque) confirmé par l'admin */
    private boolean adminPaymentConfirmed = false;
    private LocalDate adminPaymentDate;

    // ─── Source (comment connu 9antra) ─────────────────────────────────────
    /** Valeurs séparées par virgule : RESEAUX_SOCIAUX, AMI, GOOGLE, PUBLICITE, ANCIEN_STAGIAIRE, AUTRE */
    @Column(length = 500)
    private String heardFrom;

    /** Texte libre si "Autre" sélectionné */
    @Column(length = 500)
    private String heardFromOther;

    // ─── Engagement ────────────────────────────────────────────────────────
    private boolean termsAccepted = false;
    private LocalDate termsAcceptedAt;

    // ─── Statut onboarding ─────────────────────────────────────────────────
    private boolean onboardingCompleted = false;
    private LocalDate completedAt;

    // ─── Attestation PDF ───────────────────────────────────────────────────────
    /** URL Cloudinary du PDF "Attestation de Stage" généré lors de la clôture */
    @Column(length = 1000)
    private String attestationPdfUrl;

    // ─── Timestamps ────────────────────────────────────────────────────────────
    private LocalDate createdAt;

    // ─── Encadrant / Formateur Assigné ─────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    private User supervisor;

    // ─── Relation stagiaire ────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDate.now();
        }
    }
}
