package com.clinitalPlatform.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name="Tarifs")
public class Tarif {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
   private String description;
   private double price;

   private boolean active = true;


    @ManyToOne
    @JoinColumn(name = "medecin_id", referencedColumnName = "id")
    @JsonIgnore
    private Medecin medecin;

    public Tarif() {
        super();
    }
}
