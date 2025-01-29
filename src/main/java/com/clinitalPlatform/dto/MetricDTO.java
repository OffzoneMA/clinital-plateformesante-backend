package com.clinitalPlatform.dto;

import io.micrometer.core.instrument.Tag;
import lombok.Data;

import java.util.List;

@Data
public class MetricDTO {
    private String name;
    private String description;
    private List<Tag> tags;
    private double value;
}