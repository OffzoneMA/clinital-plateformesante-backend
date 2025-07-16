package com.clinitalPlatform.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.clinitalPlatform.enums.AntecedentTypeEnum;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "anticedents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Antecedents {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_anticedent;

    private AntecedentTypeEnum type;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime date = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToOne(cascade = CascadeType.ALL)
    @JsonIgnore
	private DossierMedical dossier;

    private String updatedBy;

    @PreUpdate
    protected void onUpdate() {
        if (date == null) {
            date = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        date = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
}
