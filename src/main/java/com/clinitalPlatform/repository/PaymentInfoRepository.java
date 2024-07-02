package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.PaymentInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentInfoRepository extends JpaRepository<PaymentInfo,Long> {
}
