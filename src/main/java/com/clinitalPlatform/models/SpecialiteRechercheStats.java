package com.clinitalPlatform.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "specialite_recherche_stats")
@Data
public class SpecialiteRechercheStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "specialite_id")
    private Specialite specialite;

    @Column(name = "total_recherches")
    private Integer totalRecherches = 0;
}
