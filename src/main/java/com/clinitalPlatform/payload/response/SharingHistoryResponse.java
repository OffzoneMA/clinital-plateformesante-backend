package com.clinitalPlatform.payload.response;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class SharingHistoryResponse {
   long id;
    @NotBlank
    long user;
    @NotBlank
    long patient;
    @NotBlank
    long medecin;
    @NotBlank
    long dossier;
    @NotBlank
    LocalDateTime sharingdate; 
}
