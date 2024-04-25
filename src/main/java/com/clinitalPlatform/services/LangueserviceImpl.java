package com.clinitalPlatform.services;

import com.clinitalPlatform.models.Langue;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.repository.LangueRepository;
import com.clinitalPlatform.repository.MedecinRepository;
import com.clinitalPlatform.services.interfaces.LangueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LangueserviceImpl implements LangueService {

    @Autowired
    private LangueRepository langueRepository;
    @Autowired
    private MedecinRepository medecinRepository;

    @Override
    public Langue findById(Long id) {
        Optional<Langue> optionalLangue = langueRepository.findById(id);
        return optionalLangue.orElse(null);
    }



    @Override
    public List<Medecin> findMedecinsByLangueId(Long langueId) throws Exception {
        // Récupérer l'objet Langue correspondant à l'ID spécifié
        Langue langue = langueRepository.findById(langueId)
                .orElseThrow(() -> new Exception("Langue not found for ID: " + langueId));

        // Retourner la liste des médecins associés à cette langue
        return langue.getMedecins();
    }


    public List<Langue> findAll(){
        return langueRepository.findAll();
    }


    public Langue save(Langue langue) {
        return langueRepository.save(langue);
    }

    public void delete(Langue langue) {
        // Supprimer les associations de la table langue_medecin avant de supprimer la langue
        langue.getMedecins().forEach(medecin -> medecin.getLangues().remove(langue));
        langueRepository.delete(langue);
    }

}
