package com._antra.the_bridge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate sessionDate;

    private LocalTime startTime;

    private Integer duration;

    private String location;

    private String meetingLink;

    @ManyToOne
    @JoinColumn(name = "phase_id")
    private Phase phase;

    private boolean closed = false;

    @OneToMany(mappedBy = "session")
    private List<Attendance> attendances = new ArrayList<>();
}
