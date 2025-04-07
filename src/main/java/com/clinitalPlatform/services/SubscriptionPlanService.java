package com.clinitalPlatform.services;

import com.clinitalPlatform.models.SubscriptionPlan;
import com.clinitalPlatform.repository.SubscriptionPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionPlanService {
    @Autowired
    private SubscriptionPlanRepository planRepository;

    public List<SubscriptionPlan> getAllPlans() {
        return planRepository.findAll();
    }

    public SubscriptionPlan getPlanByName(String name) {
        Optional<SubscriptionPlan> plan = planRepository.findByName(name);
        return plan.orElse(null);
    }
}

