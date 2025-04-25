package com.clinitalPlatform.controllers;

import com.clinitalPlatform.models.Langue;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.payload.request.LangueRequest;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.repository.LangueRepository;
import com.clinitalPlatform.services.LangueserviceImpl;
import com.clinitalPlatform.services.MedecinServiceImpl;
import com.clinitalPlatform.services.interfaces.LangueService;
import com.clinitalPlatform.util.GlobalVariables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
@RequestMapping("/api/langues")
public class LangueController {

    @Autowired
    private LangueserviceImpl langueservice;

    @Autowired
    private MedecinServiceImpl medecinService;
    @Autowired
    private LangueRepository langueRepository;
    @Autowired
    private GlobalVariables globalVariables;

    @GetMapping("/{id}")
    public ResponseEntity<?> getLangueById(@PathVariable Long id) {
        Langue langue = langueservice.findById(id);
        if (langue != null) {
            return ResponseEntity.ok(langue);
        } else {
            log.error("No langue found with ID: {}", id);
            return ResponseEntity.ok(new ApiResponse(false, "Aucune langue trouvée avec l'ID spécifié"));
        }
    }


    @GetMapping("/Langueslist")
    public ResponseEntity<List<Langue>> findLangue() {
        List<Langue> langues = langueservice.findAll();
        return ResponseEntity.ok(langues);
    }
    @PostMapping("/add")
    public ResponseEntity<?> addLangue(@RequestBody LangueRequest langueRequest) {
        try {

            Langue langue = new Langue();
            langue.setId_langue(langueRequest.getId_langue());
            langue.setName(langueRequest.getName());


            Langue savedLangue = langueservice.save(langue);


            return ResponseEntity.ok(savedLangue);
        } catch (Exception e) {

            log.error("Error while adding langue", e);
            return ResponseEntity.ok(new ApiResponse(false, "Erreur lors de l'ajout de la langue"));
        }
    }



    //Retourne la liste de medecins qui parles la langue selectionnées
    @GetMapping("/{langueId}/medecins")
    public ResponseEntity<List<Medecin>> getMedecinsByLangueId(@PathVariable Long langueId) {
        try {
            List<Medecin> medecins = langueservice.findMedecinsByLangueId(langueId);
            return ResponseEntity.ok(medecins);
        } catch (Exception e) {
            log.error("Error while fetching medecins for langue with ID: {}", langueId, e);
            return ResponseEntity.notFound().build();
        }
    }





    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateLangue(@PathVariable Long id, @RequestBody LangueRequest langueRequest) {
        try {
            // Vérifier si la langue avec l'ID donné existe
            Langue existingLangue = langueservice.findById(id);

            // Mettre à jour les champs de la langue existante avec les nouvelles valeurs
            existingLangue.setName(langueRequest.getName()); // Mettez à jour le nom de la langue avec le nouveau nom

            // Enregistrer la langue mise à jour
            Langue updatedLangue = langueservice.save(existingLangue);

            // Retourner la réponse avec la langue mise à jour
            return ResponseEntity.ok(updatedLangue);
        } catch (Exception e) {
            // En cas d'erreur, enregistrer l'erreur dans les logs et renvoyer une réponse avec un message d'erreur
            log.error("Error while updating langue with ID: {}", id, e);
            return ResponseEntity.ok(new ApiResponse(false, "Erreur lors de la mise à jour de la langue"));
        }
    }

    @PutMapping("/assign-langues/connected")
    public ResponseEntity<?> assignLanguesToConnectedMedecin(@RequestBody List<Long> langueIds) {
        try {

            Long userId = globalVariables.getConnectedUser().getId();

            // Récupérer l'ID du médecin connecté
            Medecin medecin = medecinService.getMedecinByUserId(userId);

            // Assigner les langues au médecin
            Medecin updatedMedecin = langueservice.assignLanguesToMedecin(langueIds, medecin.getId());

            // Retourner la réponse avec le médecin mis à jour
            return ResponseEntity.ok(updatedMedecin);
        } catch (Exception e) {
            log.error("Error while assigning langues to connected medecin", e);
            return ResponseEntity.ok(new ApiResponse(false, "Erreur lors de l'attribution des langues au médecin"));
        }
    }


}
