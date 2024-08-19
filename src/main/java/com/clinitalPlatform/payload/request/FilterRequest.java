package com.clinitalPlatform.payload.request;

import lombok.Data;

import java.util.List;

@Data
public class FilterRequest {
    private List<Long> medecinIds;
    private List<String> filters;
}
