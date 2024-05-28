package com.clinitalPlatform.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "langues")
public class Langue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_langue;
    private String name;

    @ManyToMany(mappedBy = "langues")
    @JsonIgnore
    private List<Medecin> medecins;

    public Langue() {
        super();
    }
    public Langue(Long id_langue, String name) {
        this.id_langue = id_langue;
        this.name = name;
    }


}
