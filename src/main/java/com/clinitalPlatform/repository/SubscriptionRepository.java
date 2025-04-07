package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUserIdAndIsActiveTrue(Long userId);
    Optional<Subscription> findTopByUserIdOrderByStartDateDesc(Long userId);
    boolean existsByUserIdAndIsActiveTrue(Long userId);

}

