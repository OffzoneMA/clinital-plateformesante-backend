package com.clinitalPlatform.dto;

import com.clinitalPlatform.models.Rendezvous;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConflictCheckResult {
    private boolean hasConflict;
    private String conflictMessage;
    private Rendezvous conflictingRdv;
}
