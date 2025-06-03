package com.clinitalPlatform.services;

import com.clinitalPlatform.dto.DiplomeMedecinDTO;
import com.clinitalPlatform.models.DiplomeMedecin;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.repository.DiplomeMedecinRepository;
import com.clinitalPlatform.repository.MedecinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DiplomeMedecinService {

    @Autowired
    private DiplomeMedecinRepository diplomeMedecinRepository;

    @Autowired
    private MedecinServiceImpl medService;

    @Autowired
    private MedecinRepository medecinRepository;

    public List<DiplomeMedecin> getAllDiplome(){
        return diplomeMedecinRepository.findAll();
    }

    public List<DiplomeMedecin> getAllDiplomeMedecin(Long medId) throws Exception {
        Medecin medecin = medService.findById(medId);
        return diplomeMedecinRepository.findByMedecin(medecin);
    }

    public DiplomeMedecin getDiplomeMedecinById(Long id){
        return diplomeMedecinRepository.findById(id).get();
    }

    public DiplomeMedecin updateDiplomeMedecin(Long id , DiplomeMedecinDTO dip){

        Optional<DiplomeMedecin> diplomeMedecinopt = diplomeMedecinRepository.findById(id);
        if (!diplomeMedecinopt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Diplôme non trouvé avec l'ID: " + id);
        }
        DiplomeMedecin diplomeMedecin = diplomeMedecinopt.get();

        if(dip.getNom_diplome() != null && !dip.getNom_diplome().isEmpty()) {
            diplomeMedecin.setNom_diplome(dip.getNom_diplome());
        }

        if(dip.getAnnee_obtention() != null) {
            diplomeMedecin.setAnnee_obtention(dip.getAnnee_obtention());
        }

        return diplomeMedecinRepository.save(diplomeMedecin);
    }

    public void deleteDiplomeMedecinById(Long id){
        diplomeMedecinRepository.deleteById(id);
    }

    /**
     * Crée un nouveau diplôme pour un médecin
     */
    public DiplomeMedecin createDiplome(DiplomeMedecinDTO diplomeMedecinDTO, Long medecinId) {
        Medecin medecin = medecinRepository.findById(medecinId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Médecin non trouvé avec l'ID: " + medecinId));

        DiplomeMedecin diplome = new DiplomeMedecin();
        diplome.setNom_diplome(diplomeMedecinDTO.getNom_diplome());
        diplome.setAnnee_obtention(diplomeMedecinDTO.getAnnee_obtention());
        diplome.setMedecin(medecin);

        return diplomeMedecinRepository.save(diplome);
    }

    /**
     * Crée plusieurs diplômes pour un médecin
     */
    public List<DiplomeMedecin> createDiplomes(List<DiplomeMedecinDTO> diplomeMedecinDTOs, Long medecinId) {
        Medecin medecin = medecinRepository.findById(medecinId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Médecin non trouvé avec l'ID: " + medecinId));

        List<DiplomeMedecin> diplomes = diplomeMedecinDTOs.stream()
                .map(dto -> {
                    DiplomeMedecin diplome = new DiplomeMedecin();
                    diplome.setNom_diplome(dto.getNom_diplome());
                    diplome.setAnnee_obtention(dto.getAnnee_obtention());
                    diplome.setMedecin(medecin);
                    return diplome;
                })
                .collect(Collectors.toList());

        return diplomeMedecinRepository.saveAll(diplomes);
    }

    /**
     * Supprime tous les diplômes d'un médecin
     */
    public void deleteDiplomesByMedecinId(Long medecinId) throws Exception {
        // Vérifier que le médecin existe
        if (!medecinRepository.existsById(medecinId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Médecin non trouvé avec l'ID: " + medecinId);
        }

        Medecin medecin = medService.findById(medecinId);
        List<DiplomeMedecin> diplomes = diplomeMedecinRepository.findByMedecin(medecin);
        diplomeMedecinRepository.deleteAll(diplomes);
    }

}
