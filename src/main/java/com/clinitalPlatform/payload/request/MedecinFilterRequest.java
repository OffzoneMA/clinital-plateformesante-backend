package com.clinitalPlatform.payload.request;

import lombok.Data;

import java.util.List;

@Data
public class MedecinFilterRequest {
    private List<Long> medecinIds;
    private List<String> langueFilters;
    private List<String> motifs;
    private List<String> availabilityFilters;
}
