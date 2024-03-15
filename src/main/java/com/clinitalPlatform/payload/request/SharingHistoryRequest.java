package com.clinitalPlatform.payload.request;


import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class SharingHistoryRequest {

    long id;

    //@NotNull
    //long userId;

    @NotNull
    long patientId;

    @NotNull
    long medecinId;

    @NotNull
    long dossierId;

    @DateTimeFormat
    LocalDateTime sharingdate;
}
