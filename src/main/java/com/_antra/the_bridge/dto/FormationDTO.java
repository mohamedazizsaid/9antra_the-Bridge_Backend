package com._antra.the_bridge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormationDTO {
    private Long id;
    private String title;
    private String description;
    private String category;
    private Double totalPrice;
    private List<PhaseDTO> phases;
    private List<UserDTO> trainers;
    private Integer enrollmentCount;
}
