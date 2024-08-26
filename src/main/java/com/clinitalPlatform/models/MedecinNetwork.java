package com.clinitalPlatform.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "medecin_network")
@Data
public class MedecinNetwork {

    @EmbeddedId
    private MedecinFollowersID id ;

    @ManyToOne(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @MapsId("id_medecin")
    @JoinColumn(name = "id_medecin", referencedColumnName = "id")
    @JsonBackReference
    private Medecin medecin;

    @ManyToOne(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @MapsId("id_follower")
    @JoinColumn(name = "id_follower", referencedColumnName = "id")
    @JsonBackReference
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

    public MedecinFollowersID getId() {
        return id;
    }

    public void setId(MedecinFollowersID id) {
        this.id = id;
    }

    public Medecin getMedecin() {
        return medecin;
    }

    public void setMedecin(Medecin medecin) {
        this.medecin = medecin;
    }

    public Medecin getFollower() {
        return follower;
    }

    public void setFollower(Medecin follower) {
        this.follower = follower;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MedecinNetwork that = (MedecinNetwork) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (medecin != null ? !medecin.equals(that.medecin) : that.medecin != null) return false;
        return follower != null ? follower.equals(that.follower) : that.follower == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (medecin != null ? medecin.hashCode() : 0);
        result = 31 * result + (follower != null ? follower.hashCode() : 0);
        return result;
    }
}
