package com.clinitalPlatform.dto;
import com.clinitalPlatform.models.Assistant;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.Secretaire;

import java.util.List;

public class EquipeDTO {
    private List<Medecin> medecins;
    private List<Secretaire> secretaires;
    private List<Assistant> assistants;


    // Getters and setters

    public List<Medecin> getMedecins() {
        return medecins;
    }

    public void setMedecins(List<Medecin> medecins) {
        this.medecins = medecins;
    }

    public List<Secretaire> getSecretaires() {
        return secretaires;
    }

    public void setSecretaires(List<Secretaire> secretaires) {
        this.secretaires = secretaires;
    }

    public List<Assistant> getAssistants() {
        return assistants;
    }

    public void setAssistants(List<Assistant> assistants) {
        this.assistants = assistants;
    }
}
