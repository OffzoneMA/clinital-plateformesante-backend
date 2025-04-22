package com.clinitalPlatform.models;

import com.clinitalPlatform.enums.MotifFermetureEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity
@Table(name = "motifs_fermeture")
@Data
public class MotifFermeture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MotifFermetureEnum motif;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "motifFermeture")
    @JsonIgnore
    private List<FermetureExceptionnelle> fermetureExceptionnelles;

    public MotifFermeture() {
        super();
    }

    public MotifFermeture(MotifFermetureEnum motif) {
        super();
        this.motif = motif;
    }
}
