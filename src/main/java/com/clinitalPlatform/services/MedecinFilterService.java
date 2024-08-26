package com.clinitalPlatform.services;

import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.repository.MedecinRepository;
import com.clinitalPlatform.services.interfaces.MedecinScheduleService;
import com.clinitalPlatform.services.interfaces.MedecinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedecinFilterService {

    @Autowired
    private MedecinRepository medecinRepository;
    @Autowired
    private MedecinServiceImpl medecinService;

    @Autowired
    private MedecinScheduleServiceImpl medecinScheduleService;


   /* public List<Medecin> filterMedecins(List<Long> medecinIds, List<String> langueFilters, List<String> motifs, List<String> availabilityFilters) {
        List<Medecin> filteredByLangue = medecinService.filterMedecinsByLangue(medecinIds, langueFilters);
        List<Medecin> filteredByMotif = medecinService.getMedecinsByMotif(motifs, filteredByLangue.stream().map(Medecin::getId).collect(Collectors.toList()));
        List<Medecin> filteredByAvailability = medecinScheduleService.filterMedecinsByAvailability(filteredByMotif.stream().map(Medecin::getId).collect(Collectors.toList()), availabilityFilters);

        return filteredByAvailability;
    }*/

    public List<Medecin> filterMedecins(
            List<Long> medecinIds,
            List<String> langueFilters,
            List<String> motifs,
            List<String> availabilityFilters) {

        // Récupérez les médecins correspondant aux IDs fournis
        List<Medecin> filteredMedecins = medecinRepository.findAllById(medecinIds);

        // Filtrage par langue si des langues sont spécifiées
        if (langueFilters != null && !langueFilters.isEmpty()) {
            filteredMedecins = medecinService.filterMedecinsByLangue(
                    filteredMedecins.stream().map(Medecin::getId).collect(Collectors.toList()),
                    langueFilters
            );
        }

        // Filtrage par motif si des motifs sont spécifiés
        if (motifs != null && !motifs.isEmpty() && !motifs.get(0).isEmpty()) {
            filteredMedecins = medecinService.getMedecinsByMotif(
                    motifs,
                    filteredMedecins.stream().map(Medecin::getId).collect(Collectors.toList())
            );
        }

        // Filtrage par disponibilité si des filtres de disponibilité sont spécifiés
        if (availabilityFilters != null && !availabilityFilters.isEmpty()) {
            filteredMedecins = medecinScheduleService.filterMedecinsByAvailability(
                    filteredMedecins.stream().map(Medecin::getId).collect(Collectors.toList()),
                    availabilityFilters
            );
        }

        return filteredMedecins;
    }


}
