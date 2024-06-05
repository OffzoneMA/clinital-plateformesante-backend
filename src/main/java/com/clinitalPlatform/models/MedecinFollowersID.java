package com.clinitalPlatform.models;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class MedecinFollowersID implements Serializable {

    @Column(name = "id_medecin")
    private Long id_medecin;

    @Column(name = "id_follower")
    private Long id_follower;

    public MedecinFollowersID() {
        super();
    }

    public MedecinFollowersID(Long id_medecin, Long id_follower) {
        super();
        this.id_medecin = id_medecin;
        this.id_follower = id_follower;
    }
    
}
