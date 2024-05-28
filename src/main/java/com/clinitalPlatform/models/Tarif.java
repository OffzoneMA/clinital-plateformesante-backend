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
    @ManyToOne
    @JoinColumn(name = "medecin_id", referencedColumnName = "id")
    @JsonIgnore
    private Medecin medecin;

    public Tarif() {
        super();
    }

    public Tarif(Long id, String descprtion, double price, Medecin medecin) {
        this.id = id;
        description = descprtion;
        this.price = price;
        this.medecin = medecin;
    }
}
