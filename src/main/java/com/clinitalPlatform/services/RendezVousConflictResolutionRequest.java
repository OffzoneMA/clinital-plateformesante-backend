package com.clinitalPlatform.services;

import lombok.Data;

@Data
public class RendezVousConflictResolutionRequest {

    private RendezvousRequest nouveauRdv;
    private Long ancienRdvId;
    private boolean keepNew;

}
