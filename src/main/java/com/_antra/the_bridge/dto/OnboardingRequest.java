package com._antra.the_bridge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingRequest {

    private boolean wantsInternship;

    // Stage (si wantsInternship = true)
    private String stageProjectTitle;
    private Integer stageDurationWeeks;

    // Formations sélectionnées
    private List<Long> selectedFormationIds;

    // Parrainage
    private String referralEmail;

    // Paiement
    private String paymentMode;  // COMPTANT | FACILITE | STRIPE | MAIN_A_MAIN | BANQUE
    private boolean payNow;

    // Source
    private String heardFrom;
    private String heardFromOther;

    // Engagement
    private boolean termsAccepted;
}
