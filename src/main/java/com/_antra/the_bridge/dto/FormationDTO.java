package com._antra.the_bridge.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FormationDTO {
    private Long id;
    private String title;
    private String description;
    private String category;
    private Double totalPrice;
    private List<PhaseDTO> phases;
    private List<UserDTO> trainers;
    private int enrollmentCount;
}
