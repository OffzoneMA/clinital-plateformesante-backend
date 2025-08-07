package com.clinitalPlatform.payload.response;

import com.clinitalPlatform.enums.CiviliteEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedecinResponse {
    private Long id;

    private String matricule_med;

    private String inpe;//AJOUT

    private String nom_med;

    private String prenom_med;

    private String photo_med;
    private String photo_couverture_med;
    private String description_med;
    private String contact_urgence_med;
    private CiviliteEnum civilite_med;

    // Additional fields can be added as needed

}
