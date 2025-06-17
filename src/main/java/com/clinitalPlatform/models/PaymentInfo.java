package com.clinitalPlatform.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Data
@Table(name = "payment_info")
public class PaymentInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String intituleCompte;
    private String rib;
    private String codeSwift;

    @OneToOne
    @JsonBackReference
    private Cabinet cabinet;

   //@OneToOne(mappedBy = "paymentInfo")
   //@JsonBackReference
   // private Cabinet cabinet;
    public PaymentInfo() {
        super();
    }
    public PaymentInfo(String intituleCompte, String rib, String codeSwift) {
        this.intituleCompte = intituleCompte;
        this.rib = rib;
        this.codeSwift = codeSwift;
    }


}
