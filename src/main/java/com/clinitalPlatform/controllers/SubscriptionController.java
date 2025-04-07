package com.clinitalPlatform.controllers;

import com.clinitalPlatform.models.Subscription;
import com.clinitalPlatform.services.SubscriptionService;
import com.clinitalPlatform.util.GlobalVariables;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private GlobalVariables globalVariables;

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    //Souscrire à un abonnement par ID de plan
    @PostMapping("/subscribe")
    public ResponseEntity<Subscription> subscribe(@RequestParam Long planId) {
        try {
            Long userId = globalVariables.getConnectedUser().getId();
            return ResponseEntity.ok(subscriptionService.subscribe(userId, planId));
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'abonnement : {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erreur lors de la souscription");
        }
    }

    //Souscrire à un abonnement par nom de plan
    @PostMapping("/subscribe-by-name")
    public ResponseEntity<Subscription> subscribeByPlanName(@RequestParam String planName) {
        try {
            Long userId = globalVariables.getConnectedUser().getId();
            return ResponseEntity.ok(subscriptionService.subscribeByPlanName(userId, planName));
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'abonnement avec plan '{}': {}", planName, e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan non trouvé");
        }
    }

    //Annuler un abonnement
    @PostMapping("/cancel/{subscriptionId}")
    public ResponseEntity<String> cancelSubscription(@PathVariable Long subscriptionId) {
        try {
            subscriptionService.cancelSubscription(subscriptionId);
            return ResponseEntity.ok("Abonnement annulé avec succès.");
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'annulation de l'abonnement : {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Abonnement non trouvé");
        }
    }

    //Récupérer les abonnements actifs de l'utilisateur connecté
    @GetMapping("/active")
    public ResponseEntity<List<Subscription>> getUserActiveSubscriptions() {
        try {
            Long userId = globalVariables.getConnectedUser().getId();
            return ResponseEntity.ok(subscriptionService.getUserActiveSubscriptions(userId));
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la récupération des abonnements actifs : {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucun abonnement actif trouvé");
        }
    }

    //Récupérer l’abonnement récent de l'utilisateur connecté
    @GetMapping("/latest")
    public ResponseEntity<Subscription> getUserLatestSubscription() {
        try {
            Long userId = globalVariables.getConnectedUser().getId();
            Optional<Subscription> subscription = subscriptionService.getUserLatestSubscription(userId);
            return subscription.map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucun abonnement trouvé"));
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la récupération du dernier abonnement : {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur serveur");
        }
    }

    //Vérifier si l'utilisateur connecté a un abonnement actif
    @GetMapping("/has-active")
    public ResponseEntity<Boolean> hasActiveSubscription() {
        try {
            Long userId = globalVariables.getConnectedUser().getId();
            return ResponseEntity.ok(subscriptionService.hasActiveSubscription(userId));
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la vérification de l'abonnement actif : {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur serveur");
        }
    }

    //Renouveler un abonnement
    @PostMapping("/renew")
    public ResponseEntity<Subscription> renewSubscription(@RequestParam Long planId) {
        try {
            Long userId = globalVariables.getConnectedUser().getId();
            return ResponseEntity.ok(subscriptionService.renewSubscription(userId, planId));
        } catch (Exception e) {
            LOGGER.error("Erreur lors du renouvellement de l'abonnement : {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erreur lors du renouvellement");
        }
    }

    @PostMapping("/update-payment-status")
    public ResponseEntity<String> updatePaymentStatus(
            @RequestParam String newStatus) {

        try {
            // Récupérer l'ID de l'utilisateur connecté à partir de globalVariables
            Long userId = globalVariables.getConnectedUser().getId();
            subscriptionService.updateCurrentSubscriptionStatus(userId,newStatus);
            return ResponseEntity.ok("Statut de paiement mis à jour avec succès.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body("Erreur: " + e.getMessage());
        } catch (NotFoundException e) {
            return  ResponseEntity.status(404).body(e.getMessage());
        }
    }
}
