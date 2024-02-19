package com.clinitalPlatform.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "MedecinNetwork")
@Data
public class MedecinNetwork {

    @EmbeddedId
    private MedecinFollowersID id ;

    @ManyToOne(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @MapsId("id_medecin")
    @JoinColumn(name = "id_medecin", referencedColumnName = "id")
    private Medecin medecin;

    @ManyToOne(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @MapsId("id_follower")
    @JoinColumn(name = "id_follower", referencedColumnName = "id")
    private Medecin follower;

    private String Comment;

    public MedecinNetwork() {
        super();
    }

    public MedecinNetwork(Medecin medecin, Medecin follower, String comment) {
        super();
        this.id = new MedecinFollowersID(medecin.getId(),follower.getId());
        this.medecin = medecin;
        this.follower = follower;
        this.Comment = comment;
    }
    
}
