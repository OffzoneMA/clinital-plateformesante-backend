package com.clinitalPlatform.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SharingHistoryDTO {
    
    long id;
    long user;
    long patient;
    long medecin;
    long document;
    LocalDateTime sharingdate;


}
