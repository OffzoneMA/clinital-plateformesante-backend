package com.clinitalPlatform.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private SubscriptionPlan plan;

    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isActive;
    private String paymentStatus;
}
