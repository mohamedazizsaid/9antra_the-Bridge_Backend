package com._antra.the_bridge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
