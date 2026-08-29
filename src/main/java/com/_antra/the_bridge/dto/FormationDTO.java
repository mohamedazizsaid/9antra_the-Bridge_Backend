package com._antra.the_bridge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormationDTO {
    private Long id;
    private String title;
    private String description;
    private String category;
    private Double totalPrice;
    private String status;
    private Boolean archived;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    /** Durée par défaut calculée automatiquement (en semaines) */
    private Integer defaultDurationWeeks;
    private List<PhaseDTO> phases;
    private List<UserDTO> trainers;
    private List<Integer> students; // enrolled student IDs
    private Integer enrollmentCount;
}
