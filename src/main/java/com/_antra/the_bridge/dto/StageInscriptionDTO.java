package com._antra.the_bridge.dto;

import com._antra.the_bridge.enumType.InternshipPaymentMode;
import com._antra.the_bridge.enumType.InternshipStatus;
import com._antra.the_bridge.enumType.ReferralStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StageInscriptionDTO {

    private Long id;

    // Stagiaire
    private Integer studentId;
    private String studentFirstName;
    private String studentLastName;
    private String studentEmail;
    private String studentAvatar;
    private String studentCin;

    // Type
    private boolean wantsInternship;

    // Stage
    private String stageProjectTitle;
    private Integer stageDurationWeeks;
    private String demandeStageUrl;
    private String lettreAffectationUrl;

    // Formations
    private List<Long> selectedFormationIds;
    private List<String> selectedFormationTitles;

    // Prix
    private Double originalPrice;
    private Double discountAmount;
    private String discountReason;
    private Double totalPrice;

    // Parrainage
    private String referralEmail;
    private ReferralStatus referralStatus;
    private boolean referralDiscountApplied;

    // Paiement
    private InternshipPaymentMode paymentMode;
    private boolean payNow;
    private boolean cashDiscountApplied;
    private String stripeSessionId;
    private String stripePaymentUrl;
    private boolean stripePaymentConfirmed;

    // Admin & Encadrant
    private InternshipStatus status;
    private String adminNotes;
    private boolean adminPaymentConfirmed;
    private LocalDate adminPaymentDate;
    private Integer supervisorId;
    private String supervisorFirstName;
    private String supervisorLastName;
    private String supervisorEmail;
    private String supervisorAvatar;

    // Source
    private String heardFrom;
    private String heardFromOther;

    // Engagement
    private boolean termsAccepted;
    private LocalDate termsAcceptedAt;

    // Attestation PDF (généré à la clôture)
    private String attestationPdfUrl;

    // Statut
    private boolean onboardingCompleted;
    private LocalDate completedAt;
    private LocalDate createdAt;
}
