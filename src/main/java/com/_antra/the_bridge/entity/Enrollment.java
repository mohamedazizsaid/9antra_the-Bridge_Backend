package com._antra.the_bridge.entity;
import com._antra.the_bridge.enumType.EnrollmentStatus;
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
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate enrollmentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.APPROVED;

    /** Null = parcours par défaut ; non-null = durée custom demandée par le stagiaire (en semaines) */
    private Integer customDurationWeeks;

    /** Message de motivation du stagiaire pour une durée custom */
    @Column(length = 1000)
    private String motivationMessage;

    /** Motif de rejet fourni par le formateur */
    @Column(length = 1000)
    private String rejectionReason;

    /** Date à laquelle le formateur a répondu */
    private LocalDate respondedAt;

    /** Plan personnalisé (phases, séances, jalons) au format JSON */
    @Column(columnDefinition = "TEXT")
    private String customPlan;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne
    @JoinColumn(name = "formation_id")
    private Formation formation;

    @OneToMany(mappedBy = "enrollment")
    private List<Payment> payments = new ArrayList<>();

    /**
     * Référence au combo dont cet enrollment fait partie.
     * Null si inscription individuelle (hors combo).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combo_enrollment_id")
    private ComboEnrollment comboEnrollment;
}
