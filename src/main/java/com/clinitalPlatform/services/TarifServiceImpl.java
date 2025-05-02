package com.clinitalPlatform.services;

import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.Tarif;
import com.clinitalPlatform.payload.request.TarifRequest;
import com.clinitalPlatform.repository.TarifRepository;
import com.clinitalPlatform.services.interfaces.TartifService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TarifServiceImpl implements TartifService {

    @Autowired
    private TarifRepository tarifRepository;

    @Override
    public Tarif findById(Long id) {
        Optional<Tarif> tarifOptional = tarifRepository.findById(id);
        return tarifOptional.orElse(null);
    }


    public List<Tarif> getAllTarifs() {
        return tarifRepository.findAll();
    }

    public List<Tarif> getTarifsByMedecinId(Long medecinId) {
        return tarifRepository.findByMedecinId(medecinId);
    }

    public Tarif save(TarifRequest tarifRequest , Medecin medecin) {

        Tarif tarif = new Tarif();
        tarif.setDescription(tarifRequest.getDescription());
        tarif.setPrice(tarifRequest.getPrice());
        tarif.setMedecin(medecin);
        tarif.setActive(tarifRequest.getActive());
        return tarifRepository.save(tarif);
    }

    public void deleteTarif(Long tarifId) {
        tarifRepository.deleteById(tarifId);
    }

    public Tarif updateTarif(Long tarifId, TarifRequest updatedTarifRequest , Medecin medecin) {
        Optional<Tarif> existingTarif = tarifRepository.findById(tarifId);
        if (existingTarif.isPresent()) {
            Tarif tarif = existingTarif.get();
            tarif.setDescription(updatedTarifRequest.getDescription());
            tarif.setPrice(updatedTarifRequest.getPrice());
            tarif.setMedecin(medecin);
            tarif.setActive(updatedTarifRequest.getActive());
            return tarifRepository.save(tarif);
        } else {

            return null;
        }
    }
}
