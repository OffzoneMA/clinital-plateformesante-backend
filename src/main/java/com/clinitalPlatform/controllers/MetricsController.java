package com.clinitalPlatform.controllers;

import com.clinitalPlatform.dto.MetricDTO;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final MeterRegistry meterRegistry;

    public MetricsController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @GetMapping
    public List<MetricDTO> getAllMetrics() {
        List<MetricDTO> metrics = new ArrayList<>();

        meterRegistry.getMeters().forEach(meter -> {
            MetricDTO metricDTO = new MetricDTO();
            metricDTO.setName(meter.getId().getName());
            metricDTO.setDescription(meter.getId().getDescription());
            metricDTO.setTags(meter.getId().getTags());

            // Obtenir la valeur selon le type de métrique
            if (meter instanceof Counter) {
                metricDTO.setValue(((Counter) meter).count());
            } else if (meter instanceof Gauge) {
                metricDTO.setValue(((Gauge) meter).value());
            } else if (meter instanceof Timer) {
                metricDTO.setValue(((Timer) meter).totalTime(TimeUnit.SECONDS));
            }

            metrics.add(metricDTO);
        });

        return metrics;
    }

    @GetMapping("/{metricName}")
    public MetricDTO getMetric(@PathVariable String metricName) {
        Search.in(meterRegistry)
                .name(metricName)
                .meters()
                .stream()
                .findFirst()
                .map(meter -> {
                    MetricDTO metricDTO = new MetricDTO();
                    metricDTO.setName(meter.getId().getName());
                    metricDTO.setDescription(meter.getId().getDescription());
                    metricDTO.setTags(meter.getId().getTags());
                    // Définir la valeur selon le type
                    return metricDTO;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return null;
    }

    @GetMapping("/health")
    public Map<String, Object> getHealthMetrics() {
        Map<String, Object> health = new HashMap<>();
        health.put("userCount", meterRegistry.get("app.users.active.total").gauge().value());
        health.put("appointmentCount", meterRegistry.get("app.appointments.scheduled").counter().count());
        return health;
    }
}
