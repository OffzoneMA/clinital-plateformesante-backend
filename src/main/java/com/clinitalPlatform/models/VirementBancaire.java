package com.clinitalPlatform.models;

import com.clinitalPlatform.enums.TypeMoyenPaiementEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "virement_bancaire")
@Data
public class VirementBancaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_vb;

    @Column(nullable = false)
    private String rib;

    @Column(nullable = false)
    private String codeSwift;

    @Column(nullable = false)
    private String bankName;

    @OneToOne
    @JoinColumn(name = "id_mp", referencedColumnName = "id_mp")
    @JsonIgnore
    private MoyenPaiement moyenPaiement;

    @ManyToOne
    @JoinColumn(name = "id_medecin", referencedColumnName = "id")
    private Medecin medecin;

    public VirementBancaire(String rib, String codeSwift, String bankName) {
        this.rib = rib;
        this.codeSwift = codeSwift;
        this.bankName = bankName;
    }

    public VirementBancaire() {
        super();
    }

    @PrePersist
    @PreUpdate
    private void validateVirementBancaire() {
        if (!TypeMoyenPaiementEnum.Virement.equals(moyenPaiement.getType())) {
            throw new IllegalStateException("Les détails bancaires ne peuvent être utilisés qu'avec un moyen de paiement de type VIREMENT.");
        }
    }
}
