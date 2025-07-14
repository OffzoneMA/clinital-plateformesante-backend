package com.clinitalPlatform.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Converter
public class JsonDataConverter implements AttributeConverter<Map<String, Object>, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Erreur de conversion JSON", e);
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.trim().isEmpty()) {
                return new HashMap<>(); // ou `null` selon ton besoin
            }
            return mapper.readValue(dbData, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException("Erreur de lecture JSON", e);
        }
    }
}

