package com.clinitalPlatform.models;

import com.clinitalPlatform.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // "MEDECIN", "CABINET", "CENTRE_DE_SANTE"

    @Column(nullable = false)
    private int price; // Prix en centimes (ex: 1000 = 10€)

    @Column(nullable = false)
    private int durationInMonths; // Durée de l'abonnement (ex: 1, 6, 12 mois)

    @Column(nullable = false, length = 500)
    private String description; // Description du plan

    private boolean hasPremiumFeatures; // Accès aux fonctionnalités avancées

    @ElementCollection
    private List<String> benefits; // Liste des avantages spécifiques à chaque plan

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType; // Type de paiement: MENSUEL ou ANNUEL

}

