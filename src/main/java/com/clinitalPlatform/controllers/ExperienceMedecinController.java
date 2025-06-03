package com.clinitalPlatform.controllers;

import com.clinitalPlatform.dto.ExperienceMedecinDTO;
import com.clinitalPlatform.models.ExperienceMedecin;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.services.ExperienceMedecinService;
import com.clinitalPlatform.services.ExperienceMedecinServiceImpl;
import com.clinitalPlatform.services.interfaces.MedecinService;
import com.clinitalPlatform.util.GlobalVariables;
import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/experiences")
public class ExperienceMedecinController {

    private static final Logger log = LoggerFactory.getLogger(ExperienceMedecinController.class);
    @Autowired
    private ExperienceMedecinServiceImpl service;
    @Autowired
    private GlobalVariables globalVariables;
    @Autowired
    private MedecinService medecinService;

    @PostMapping
    public ExperienceMedecinDTO create(@RequestBody ExperienceMedecinDTO dto) {
        return service.createExperience(dto);
    }

    @PostMapping("/add/connected")
    public ResponseEntity<?> createForConnectedMedecin(@RequestBody ExperienceMedecinDTO dto) {
        try {
            Long userId = globalVariables.getConnectedUser().getId();
            Medecin medecin = medecinService.getMedecinByUserId(userId);

            if (medecin == null) {
                return ResponseEntity.status(404).body("Médecin non trouvé");
            }

            dto.setMedecinId(medecin.getId());
            ExperienceMedecinDTO saved = service.createExperience(dto);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur : " + e.getMessage());
        }
    }


    @PostMapping("/add/batch")
    public List<ExperienceMedecinDTO> createMultiple(@RequestBody List<ExperienceMedecinDTO> dtos) {
        return service.createMultipleExperiences(dtos);
    }

    @PostMapping("/add/connected/batch")
    public ResponseEntity<?> createMultipleForConnectedMedecin(@RequestBody List<ExperienceMedecinDTO> dtos) {
        try {
            log.info("RAW JSON received: {}", new ObjectMapper().writeValueAsString(dtos));

            log.info("Create multiple experiences" + dtos);
            Long userId = globalVariables.getConnectedUser().getId();
            Medecin medecin = medecinService.getMedecinByUserId(userId);

            if (medecin == null) {
                return ResponseEntity.status(404).body("Médecin non trouvé");
            }

            List<ExperienceMedecinDTO> completedDtos = dtos.stream().peek(dto -> dto.setMedecinId(medecin.getId())).collect(Collectors.toList());

            List<ExperienceMedecinDTO> saved = service.createMultipleExperiences(completedDtos);
            return ResponseEntity.ok("Expériences ajoutées avec succès.");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur : " + e.getMessage());
        }
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ExperienceMedecinDTO dto) {
        try {
            ExperienceMedecinDTO updatedExperience = service.updateExperience(id, dto);
            if (updatedExperience == null) {
                return ResponseEntity.status(404).body("Expérience non trouvée");
            }
            return ResponseEntity.ok(updatedExperience);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur : " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.deleteExperience(id);
            return ResponseEntity.ok().build();
        }catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur : " + e.getMessage());
        }
    }

    @GetMapping("/byMedId/{medecinId}")
    public List<ExperienceMedecin> getByMedecin(@PathVariable Long medecinId) {
        return service.getAllExperiencesByMedecin(medecinId);
    }

    @GetMapping("/byMed")
    public ResponseEntity<?> getByConnectedMedecin() {
        // Assuming you have a method to get the connected medecin ID
        try {
            Long userId = globalVariables.getConnectedUser().getId();
            Medecin medecin = medecinService.getMedecinByUserId(userId);
            if (medecin == null) {
                return ResponseEntity.status(404).body("Medecin not found");
            }

            List<ExperienceMedecin> experiences = service.getAllExperiencesByMedecin(medecin.getId());

            if (experiences.isEmpty()) {
                return ResponseEntity.status(404).body("No experiences found for this medecin");
            }

            return ResponseEntity.ok(experiences);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}

