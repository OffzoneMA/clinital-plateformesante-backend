package com.clinitalPlatform.controllers;

import com.clinitalPlatform.models.SubscriptionPlan;
import com.clinitalPlatform.services.SubscriptionPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class SubscriptionPlanController {
    @Autowired
    private SubscriptionPlanService planService;

    @GetMapping("/all")
    public ResponseEntity<List<SubscriptionPlan>> getAllPlans() {
        return ResponseEntity.ok(planService.getAllPlans());
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<SubscriptionPlan> getPlanByName(@PathVariable String name) {
        SubscriptionPlan plan = planService.getPlanByName(name);
        return plan != null ? ResponseEntity.ok(plan) : ResponseEntity.notFound().build();
    }
}

