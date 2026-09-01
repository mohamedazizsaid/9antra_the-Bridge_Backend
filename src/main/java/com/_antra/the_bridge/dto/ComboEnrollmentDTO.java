package com._antra.the_bridge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO pour ComboEnrollment — représente un panier de formations avec remise progressive.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComboEnrollmentDTO {

    private Long id;

    // Stagiaire
    private Integer studentId;
    private String studentFirstName;
    private String studentLastName;
    private String studentEmail;
    private String studentAvatar;

    // Formations incluses dans le combo
    private List<FormationDTO> formations;

    // Tarification
    private Double totalPrice;       // somme brute des prix des formations
    private Double discountPercent;  // remise calculée (0-40%)
    private Double finalPrice;       // totalPrice * (1 - discountPercent/100)

    // Statut
    private String status;           // PENDING_PAYMENT | ACTIVE | COMPLETED | CANCELLED

    // Dates
    private LocalDate createdAt;
    private LocalDate paidAt;

    // Stripe & Reçu
    private String stripeSessionId;
    private String stripeCheckoutUrl; // URL renvoyée au frontend pour redirection
    private String receiptRef;        // numéro de reçu unique

    private String note;

    // Enrollments individuels (générés après paiement)
    private List<EnrollmentDTO> enrollments;
}
