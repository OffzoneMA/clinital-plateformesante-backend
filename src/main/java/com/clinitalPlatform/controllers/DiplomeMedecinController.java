package com.clinitalPlatform.controllers;

import com.clinitalPlatform.dto.DiplomeMedecinDTO;
import com.clinitalPlatform.models.DiplomeMedecin;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.repository.DiplomeMedecinRepository;
import com.clinitalPlatform.services.DiplomeMedecinService;
import com.clinitalPlatform.services.MedecinServiceImpl;
import com.clinitalPlatform.util.GlobalVariables;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Comparator;
import java.util.Optional;
import java.util.logging.Logger;

@CrossOrigin(origins = "*" , maxAge = 3600)
@RestController
@RequestMapping ("/api/diplome")
public class DiplomeMedecinController {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(DiplomeMedecinController.class);
    @Autowired
    private DiplomeMedecinRepository diplomeMedecinRepository;

    @Autowired
    private DiplomeMedecinService diplomeMedecinService;
    @Autowired
    private GlobalVariables globalVariables;
    @Autowired
    private MedecinServiceImpl medecinServiceImpl;

    @GetMapping
    public ResponseEntity<?> getAllDiplomeMedecin(){
        try {
            return new ResponseEntity<>(diplomeMedecinRepository.findAll(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/byMed")
    public ResponseEntity<?> getDiplomeMedecinByMe() {
        try {
            Long userid = globalVariables.getConnectedUser().getId();
            Medecin medecin = medecinServiceImpl.getMedecinByUserId(userid);
            List<DiplomeMedecin> diplomeMedecins = diplomeMedecinRepository.findByMedecin(medecin);

            diplomeMedecins.sort(Comparator.comparing(d -> {
                try {
                    return Integer.parseInt(d.getAnnee_obtention());
                } catch (NumberFormatException e) {
                    return Integer.MAX_VALUE; // Pour mettre les valeurs invalides en dernier
                }
            }));

            return new ResponseEntity<>(diplomeMedecins, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/byId/{id}")
    public ResponseEntity<?> getDiplomeMedecinById(@PathVariable Long id)  {
       try {
           Optional<DiplomeMedecin> diplomeMedecin =  diplomeMedecinRepository.findById(id);
           DiplomeMedecin diplome =  diplomeMedecin.orElse(null);
           return new ResponseEntity<>( diplomeMedecin, HttpStatus.OK);
       } catch (Exception e) {
           return new ResponseEntity<>( e.getMessage(), HttpStatus.NOT_FOUND);
       }
    }

    @PutMapping("/update/{id}")
        public ResponseEntity<?> updateDiplome(@PathVariable Long id, @RequestBody DiplomeMedecin diplomeMedecin) {
        try{
            Optional<DiplomeMedecin> diplomeMedecin1 = diplomeMedecinRepository.findById(id);
            if(diplomeMedecin1.isPresent()){
                DiplomeMedecin diploMedecin = diplomeMedecinService.updateDiplomeMedecin(id ,diplomeMedecin);
                return new ResponseEntity<>(diplomeMedecin, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteDiplomeMedecin(@PathVariable Long id) {
       try {
           diplomeMedecinRepository.deleteById(id);
           return new ResponseEntity<>(HttpStatus.OK);
       } catch (Exception e) {
           return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }

    /**
     * Crée un nouveau diplôme pour un médecin
     */
    @PostMapping("/add/{medecinId}/diplomes")
    public ResponseEntity<DiplomeMedecin> createDiplome(
            @PathVariable Long medecinId,
            @RequestBody DiplomeMedecinDTO diplomeMedecinDTO) {

        DiplomeMedecin newDiplome = diplomeMedecinService.createDiplome(diplomeMedecinDTO, medecinId);
        return new ResponseEntity<>(newDiplome, HttpStatus.CREATED);
    }

    @PostMapping("/add/connected")
    public ResponseEntity<?> connectDiplomeMedecin(@RequestBody DiplomeMedecinDTO diplomeMedecinDTO) {
        try {
            Long userid = globalVariables.getConnectedUser().getId();
            Medecin medecin = medecinServiceImpl.getMedecinByUserId(userid);

            DiplomeMedecin newDiplome = diplomeMedecinService.createDiplome(diplomeMedecinDTO, medecin.getId());
            return new ResponseEntity<>(newDiplome, HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Crée plusieurs diplômes pour un médecin
     */
    @PostMapping("/add/{medecinId}/diplomes/batch")
    public ResponseEntity<List<DiplomeMedecin>> createDiplomes(
            @PathVariable Long medecinId,
            @RequestBody List<DiplomeMedecinDTO> diplomeMedecinDTOs) {

        List<DiplomeMedecin> newDiplomes = diplomeMedecinService.createDiplomes(diplomeMedecinDTOs, medecinId);
        return new ResponseEntity<>(newDiplomes, HttpStatus.CREATED);
    }

    @PostMapping("/add/connected/batch")
    public ResponseEntity<?> createDiplomes(@RequestBody List<DiplomeMedecinDTO> diplomeMedecinDTO) {
        try {
            Long userid = globalVariables.getConnectedUser().getId();
            Medecin medecin = medecinServiceImpl.getMedecinByUserId(userid);
            //Log data
            log.info("Diplome medecin data" + diplomeMedecinDTO);
            List<DiplomeMedecin> newDiplomes = diplomeMedecinService.createDiplomes(diplomeMedecinDTO, medecin.getId());

            return new ResponseEntity<>(newDiplomes, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la création des diplômes : " + e.getMessage());
        }
    }
}
