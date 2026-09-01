package com._antra.the_bridge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente un panier de formations groupées (combo) souscrit par un stagiaire.
 * Un seul paiement Stripe couvre toutes les formations du combo avec une remise progressive.
 * Une fois payé, des Enrollment individuels (APPROVED) sont générés automatiquement.
 */
@Entity
@Table(name = "combo_enrollment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComboEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Stagiaire ayant créé ce combo */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    /** Formations incluses dans le combo */
    @ManyToMany
    @JoinTable(
            name = "combo_enrollment_formations",
            joinColumns = @JoinColumn(name = "combo_enrollment_id"),
            inverseJoinColumns = @JoinColumn(name = "formation_id")
    )
    private List<Formation> formations = new ArrayList<>();

    /** Somme brute des prix des formations sélectionnées */
    private Double totalPrice;

    /**
     * Pourcentage de remise appliqué (calculé à partir du nombre de formations) :
     * 2 formations = 10%, +5% par formation supplémentaire, max 40%
     */
    private Double discountPercent;

    /** Prix final après remise : totalPrice * (1 - discountPercent / 100) */
    private Double finalPrice;

    /**
     * Statut du combo :
     * PENDING_PAYMENT → en attente de paiement Stripe
     * ACTIVE          → paiement validé, inscriptions générées
     * COMPLETED       → toutes les formations terminées
     * CANCELLED       → combo annulé avant paiement
     */
    @Column(nullable = false)
    private String status = "PENDING_PAYMENT";

    /** Date de création du combo */
    private LocalDate createdAt;

    /** Date à laquelle le paiement a été confirmé */
    private LocalDate paidAt;

    /** ID de la session Stripe Checkout */
    private String stripeSessionId;

    /** Numéro de reçu unique généré à la création du combo */
    @Column(unique = true)
    private String receiptRef;

    /** Note optionnelle du stagiaire lors de la création */
    @Column(length = 1000)
    private String note;

    /**
     * Enrollments individuels générés après paiement (1 par formation).
     * null si pas encore payé.
     */
    @OneToMany(mappedBy = "comboEnrollment", cascade = CascadeType.ALL)
    private List<Enrollment> enrollments = new ArrayList<>();

    // ─── Helper : calcul de la remise selon le nombre de formations ───────────

    /**
     * Calcule le pourcentage de remise en fonction du nombre de formations :
     * - 1 formation  : 0%  (pas de remise)
     * - 2 formations : 10%
     * - 3 formations : 15%
     * - n formations : min(10 + (n-2)*5, 40)%
     */
    public static double computeDiscountPercent(int formationCount) {
        if (formationCount < 2) return 0.0;
        return Math.min(10.0 + (formationCount - 2) * 5.0, 40.0);
    }
}
