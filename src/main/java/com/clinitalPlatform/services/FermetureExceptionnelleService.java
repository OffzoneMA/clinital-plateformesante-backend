package com.clinitalPlatform.services;

import com.clinitalPlatform.dto.FermetureDTO;
import com.clinitalPlatform.enums.MotifFermetureEnum;
import com.clinitalPlatform.models.FermetureExceptionnelle;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.MotifFermeture;
import com.clinitalPlatform.repository.FermetureExceptionnelleRepository;
import com.clinitalPlatform.repository.MedecinRepository;
import com.clinitalPlatform.repository.MotifFermetureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class FermetureExceptionnelleService {

    @Autowired
    private FermetureExceptionnelleRepository fermetureRepo;

    @Autowired
    private MedecinRepository medecinRepo;

    @Autowired
    private MotifFermetureRepository motifRepo;


    public List<FermetureExceptionnelle> getFermeturesParMedecin(Long medecinId) {
        return fermetureRepo.findAllByMedecinId(medecinId);
    }

    public FermetureExceptionnelle ajouterFermetureParIds(FermetureDTO fermetureDTO, List<Long> motifIds) {
        // Récupérer le médecin à partir de son ID
        Medecin medecin = medecinRepo.findById(fermetureDTO.getMedecinId())
                .orElseThrow(() -> new RuntimeException("Médecin introuvable"));

        // Récupérer les motifs de fermeture à partir des IDs
        List<MotifFermeture> motifs = motifRepo.findAllById(motifIds);

        // Vérifier les conflits de fermeture pour le même médecin
        boolean conflit = fermetureRepo.existsByMedecinIdAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(
                medecin.getId(),
                fermetureDTO.getDateFin(),      // nouvelle date fin >= début existante
                fermetureDTO.getDateDebut()     // nouvelle date début <= fin existante
        );

        if (conflit) {
            throw new RuntimeException("Une fermeture existe déjà pour cette période.");
        }

        // Créer la fermeture
        FermetureExceptionnelle fermeture = new FermetureExceptionnelle();
        fermeture.setDateDebut(fermetureDTO.getDateDebut());
        fermeture.setDateFin(fermetureDTO.getDateFin());
        fermeture.setMedecin(medecin);
        fermeture.setMotifFermeture(motifs);

        return fermetureRepo.save(fermeture);
    }

    public FermetureExceptionnelle updateFermeture(Long id, FermetureDTO fermetureDTO) {
        FermetureExceptionnelle fermeture = fermetureRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Fermeture introuvable"));

        // Mettre à jour les champs de la fermeture
        fermeture.setDateDebut(fermetureDTO.getDateDebut());
        fermeture.setDateFin(fermetureDTO.getDateFin());

        // Sauvegarder les modifications
        List<MotifFermeture> motifs = motifRepo.findAllById(fermetureDTO.getMotifIds());
        fermeture.setMotifFermeture(motifs);
        return fermetureRepo.save(fermeture);
    }

    public void supprimerFermeture(Long id) {
        fermetureRepo.deleteById(id);
    }

    public List<FermetureExceptionnelle> getAllFermetures() {
        return fermetureRepo.findAll();
    }

    public FermetureExceptionnelle getFermetureById(Long id) {
        return fermetureRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Fermeture introuvable"));
    }
}

