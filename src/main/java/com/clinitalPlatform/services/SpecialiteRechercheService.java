package com.clinitalPlatform.services;

import com.clinitalPlatform.models.Specialite;
import com.clinitalPlatform.models.SpecialiteRechercheStats;
import com.clinitalPlatform.repository.SpecialiteRechercheStatsRepository;
import com.clinitalPlatform.repository.SpecialiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class SpecialiteRechercheService {

    @Autowired
    private SpecialiteRechercheStatsRepository statsRepository;

    @Autowired
    private SpecialiteRepository specialiteRepository;

    @Transactional
    public void incrementerRecherche(Long specialiteId) {
        // Vérifier si la SpecialiteRechercheStats existe déjà
        Optional<SpecialiteRechercheStats> existing = statsRepository.findBySpecialiteId(specialiteId);

        // Récupérer la Specialite directement une fois, au lieu de deux appels
        Specialite specialite = specialiteRepository.getSpecialiteById(specialiteId);

        // Si la SpecialiteRechercheStats existe, incrémenter
        if (existing.isPresent()) {
            SpecialiteRechercheStats existingSpec = existing.get();
            existingSpec.setTotalRecherches(existingSpec.getTotalRecherches() + 1);
            statsRepository.save(existingSpec);
        } else {
            // Si la SpecialiteRechercheStats n'existe pas, en créer une nouvelle
            SpecialiteRechercheStats newStat = new SpecialiteRechercheStats();
            if (specialite != null) {
                newStat.setSpecialite(specialite);
            } else {
                // Gestion du cas où la spécialité est null (facultatif, mais important pour éviter les erreurs)
                throw new IllegalArgumentException("Specialite not found for id: " + specialiteId);
            }
            newStat.setTotalRecherches(1); // Initialiser à 1 car c'est la première recherche
            statsRepository.save(newStat); // Sauvegarder la nouvelle entrée
        }
    }


    public List<SpecialiteRechercheStats> getTopSpecialites(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return statsRepository.findTopSpecialites(pageable);
    }
}
