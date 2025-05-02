package com.clinitalPlatform.payload.request;

import com.clinitalPlatform.models.Medecin;
import lombok.Data;

@Data
public class TarifRequest {
    private Long id;
    private String description;
    private double price;
    private Boolean active = true;
}
