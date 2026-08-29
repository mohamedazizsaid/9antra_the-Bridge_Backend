package com._antra.the_bridge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Formation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;

    private String category;

    private Double totalPrice;

    private String status = "PLANIFIEE"; // ACTIVE | TERMINEE | PLANIFIEE

    private boolean archived = false;

    private LocalDate startDate;

    private LocalDate endDate;

    @OneToMany(mappedBy = "formation", cascade = CascadeType.ALL)
    private List<Phase> phases = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "formation_trainers",
            joinColumns = @JoinColumn(name = "formation_id"),
            inverseJoinColumns = @JoinColumn(name = "trainer_id")
    )
    private List<User> trainers = new ArrayList<>();

    @OneToMany(mappedBy = "formation")
    private List<Enrollment> enrollments = new ArrayList<>();

    /**
     * Durée par défaut calculée automatiquement à partir de startDate / endDate.
     * Retourne null si l'une des deux dates est absente.
     */
    @Transient
    public Integer getDefaultDurationWeeks() {
        if (startDate == null || endDate == null) return null;
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        return (int) Math.max(1, Math.round(days / 7.0));
    }
}
