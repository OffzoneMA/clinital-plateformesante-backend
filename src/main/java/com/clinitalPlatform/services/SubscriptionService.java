package com.clinitalPlatform.services;

import com.clinitalPlatform.models.Subscription;
import com.clinitalPlatform.models.SubscriptionPlan;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.repository.SubscriptionRepository;
import com.clinitalPlatform.repository.SubscriptionPlanRepository;
import com.clinitalPlatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionPlanRepository planRepository;

    @Autowired
    private UserRepository userRepository;

    // Souscrire à un abonnement
    public Subscription subscribe(Long userId, Long planId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan non trouvé"));

        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(plan.getDurationInMonths()))
                .isActive(true)
                .paymentStatus("PENDING")
                .build();

        return subscriptionRepository.save(subscription);
    }

    public Subscription subscribeByPlanName(Long userId, String planName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        SubscriptionPlan plan = planRepository.findByName(planName).orElseThrow(() -> new RuntimeException("Plan non trouvé"));

        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(plan.getDurationInMonths()))
                .isActive(true)
                .paymentStatus("PENDING")
                .build();

        return subscriptionRepository.save(subscription);
    }


    // Annuler un abonnement
    public void cancelSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Abonnement non trouvé"));

        subscription.setActive(false);
        subscriptionRepository.save(subscription);
    }

    // Récupérer les abonnements actifs d'un utilisateur
    public List<Subscription> getUserActiveSubscriptions(Long userId) {
        return subscriptionRepository.findByUserIdAndIsActiveTrue(userId);
    }

    // Obtenir l'abonnement récent d'un utilisateur
    public Optional<Subscription> getUserLatestSubscription(Long userId) {
        return subscriptionRepository.findTopByUserIdOrderByStartDateDesc(userId);
    }

    // Vérifier si un utilisateur a un abonnement actif
    public boolean hasActiveSubscription(Long userId) {
        return subscriptionRepository.existsByUserIdAndIsActiveTrue(userId);
    }

    public List<Subscription> getUserSubscriptions(Long userId) {
        return subscriptionRepository.findByUserIdAndIsActiveTrue(userId);
    }

    // Renouveler un abonnement
    public Subscription renewSubscription(Long userId, Long planId) {
        Subscription latestSubscription = getUserLatestSubscription(userId)
                .orElseThrow(() -> new RuntimeException("Aucun abonnement trouvé"));

        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan non trouvé"));

        Subscription newSubscription = Subscription.builder()
                .user(latestSubscription.getUser())
                .plan(plan)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(plan.getDurationInMonths()))
                .isActive(true)
                .paymentStatus("EN_COURS")
                .build();

        return subscriptionRepository.save(newSubscription);
    }

    // Mettre à jour le statut de paiement d'un abonnement
    public Subscription updatePaymentStatus(Long subscriptionId, String newStatus) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Abonnement non trouvé"));

        subscription.setPaymentStatus(newStatus);
        return subscriptionRepository.save(subscription);
    }

    public Subscription updateCurrentSubscriptionStatus(Long userId, String newStatus) {
        // Récupérer l'abonnement actif le plus récent de l'utilisateur
        Optional<Subscription> optionalSubscription = getUserLatestSubscription(userId);

        // Vérifier si l'abonnement existe
        if (optionalSubscription.isPresent()) {
            Subscription subscription = optionalSubscription.get();
            subscription.setPaymentStatus(newStatus);  // Mettre à jour le statut de paiement
            return subscriptionRepository.save(subscription);  // Sauvegarder l'abonnement mis à jour
        } else {
            throw new RuntimeException("Aucun abonnement actif trouvé pour cet utilisateur");
        }
    }


}
