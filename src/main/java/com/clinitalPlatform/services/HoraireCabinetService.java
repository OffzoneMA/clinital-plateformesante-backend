package com.clinitalPlatform.services;

import com.clinitalPlatform.models.Cabinet;
import com.clinitalPlatform.models.HoraireCabinet;
import com.clinitalPlatform.payload.request.HoraireCabinetRequest;
import com.clinitalPlatform.repository.HoraireCabinetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class HoraireCabinetService {

    @Autowired
    private HoraireCabinetRepository horaireCabinetRepository;

    @Autowired
    private CabinetServiceImpl cabinetService;

    public final Logger LOGGER= LoggerFactory.getLogger(this.getClass());

    // Méthode pour récupérer les horaires du cabinet
    public List<HoraireCabinet> getHoraireCabinets(Long cabinetId) {
        return horaireCabinetRepository.findByCabinetId(cabinetId);
    }

    // Méthode pour ajouter un horaire de cabinet
    public List<HoraireCabinet> addHoraireCabinet(HoraireCabinetRequest horaireCabinetRequest) throws Exception {
        if(horaireCabinetRequest.getTimeSlots() == null) {
            throw new IllegalArgumentException("Les créneaux horaires ne peuvent pas être nuls");
        }
        Optional<Cabinet> cabinet = cabinetService.findById(horaireCabinetRequest.getCabinetId());
        if (cabinet.isEmpty()) {
            throw new Exception("Cabinet introuvable avec l'ID : " + horaireCabinetRequest.getCabinetId());
        }
        List<HoraireCabinet> horaireCabinets = new ArrayList<>();
        for (HoraireCabinetRequest.TimeSlot slot : horaireCabinetRequest.getTimeSlots()) {
            HoraireCabinet h = new HoraireCabinet();
            h.setDay(horaireCabinetRequest.getDay());
            h.setStartTime(slot.getStartTime());
            h.setEndTime(slot.getEndTime());
            h.setFerme(false);
            h.setCabinet(cabinet.get());
            horaireCabinets.add(h);
        }

        // Enregistrer tous les horaires de cabinet
        return horaireCabinetRepository.saveAll(horaireCabinets);
    }

    // Méthode pour supprimer un horaire de cabinet par son ID
    public void deleteHoraireCabinet(Long id) throws Exception {
        Optional<HoraireCabinet> horaireCabinet = horaireCabinetRepository.findById(id);
        if (horaireCabinet.isEmpty()) {
            throw new Exception("Horaire de cabinet introuvable avec l'ID : " + id);
        }
        horaireCabinetRepository.delete(horaireCabinet.get());
    }

    // Méthode pour mettre à jour un horaire de cabinet
    public HoraireCabinet updateHoraireCabinet(Long id, HoraireCabinetRequest horaireCabinetRequest) throws Exception {
        Optional<HoraireCabinet> existingHoraire = horaireCabinetRepository.findById(id);
        if (existingHoraire.isEmpty()) {
            throw new Exception("Horaire de cabinet introuvable avec l'ID : " + id);
        }

        HoraireCabinet horaireCabinet = existingHoraire.get();
        horaireCabinet.setDay(horaireCabinetRequest.getDay());
        horaireCabinet.setFerme(horaireCabinetRequest.isFerme());

        // Mettre à jour les créneaux horaires
        List<HoraireCabinetRequest.TimeSlot> timeSlots = horaireCabinetRequest.getTimeSlots();
        if (timeSlots != null && !timeSlots.isEmpty()) {
            horaireCabinet.setStartTime(timeSlots.get(0).getStartTime());
            horaireCabinet.setEndTime(timeSlots.get(0).getEndTime());
        }

        return horaireCabinetRepository.save(horaireCabinet);
    }

    public void updateHorairesCabinet(Long cabinetId, List<HoraireCabinetRequest> requests) throws Exception {
        Optional<Cabinet> cabinet = cabinetService.findById(cabinetId);

        if(cabinet.isEmpty()) {
            throw new Exception("Cabinet introuvable avec l'ID : " + cabinetId);
        }

        List<HoraireCabinet> toSave = new ArrayList<>();

        LOGGER.info("Horaires requests : {}", requests);

        for (HoraireCabinetRequest req : requests) {
            if (req.getTimeSlots() != null) {
                for (HoraireCabinetRequest.TimeSlot slot : req.getTimeSlots()) {
                    if (slot.getStartTime() == null || slot.getEndTime() == null) continue;

                    HoraireCabinet horaire;

                    if (slot.getId() != null && slot.getId() > 0) {
                        // Mettre à jour un horaire existant
                        horaire = horaireCabinetRepository.findById(slot.getId())
                                .orElseThrow(() -> new RuntimeException("Horaire non trouvé : ID " + slot.getId()));
                    } else {
                        // Créer un nouveau créneau
                        horaire = new HoraireCabinet();
                        horaire.setCabinet(cabinet.get());
                    }


                    horaire.setDay(req.getDay());
                    horaire.setStartTime(slot.getStartTime());
                    horaire.setEndTime(slot.getEndTime());
                    horaire.setFerme(req.isFerme());

                    toSave.add(horaire);
                }
            }
        }

        horaireCabinetRepository.saveAll(toSave);
    }

}
