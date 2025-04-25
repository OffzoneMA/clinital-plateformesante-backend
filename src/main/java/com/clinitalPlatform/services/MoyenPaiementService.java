package com.clinitalPlatform.services;

import com.clinitalPlatform.dto.MoyenPaiementDTO;
import com.clinitalPlatform.dto.VirementBancaireDTO;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.MoyenPaiement;
import com.clinitalPlatform.models.VirementBancaire;
import com.clinitalPlatform.enums.TypeMoyenPaiementEnum;
import com.clinitalPlatform.repository.MedecinRepository;
import com.clinitalPlatform.repository.MoyenPaiementRepository;
import com.clinitalPlatform.repository.VirementBancaireRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MoyenPaiementService {

    @Autowired
    private MoyenPaiementRepository moyenPaiementRepository;

    @Autowired
    private VirementBancaireRepository virementBancaireRepository;

    @Autowired
    private MedecinRepository medecinRepository;

    @Autowired
    private ModelMapper modelMapper;

    // Ajouter un moyen de paiement
    public MoyenPaiementDTO createMoyenPaiement(TypeMoyenPaiementEnum type) {
        MoyenPaiement moyenPaiement = new MoyenPaiement(type);
        MoyenPaiement savedMoyenPaiement = moyenPaiementRepository.save(moyenPaiement);
        return modelMapper.map(savedMoyenPaiement, MoyenPaiementDTO.class);
    }

    // Récupérer tous les moyens de paiement
    public List<MoyenPaiementDTO> getAllMoyensPaiement() {
        List<MoyenPaiement> moyensPaiement = moyenPaiementRepository.findAll();
        return moyensPaiement.stream()
                .map(moyenPaiement -> modelMapper.map(moyenPaiement, MoyenPaiementDTO.class))
                .collect(Collectors.toList());
    }

    // Récupérer les moyens de paiement par ID
    public Optional<MoyenPaiementDTO> getMoyenPaiementById(Long id) {
        Optional<MoyenPaiement> moyenPaiement = moyenPaiementRepository.findById(id);
        return moyenPaiement.map(mp -> modelMapper.map(mp, MoyenPaiementDTO.class));
    }

    // Supprimer un moyen de paiement
    public void deleteMoyenPaiement(Long id) {
        moyenPaiementRepository.deleteById(id);
    }

    public Medecin addPaymentMethodsToMedecin(Long medecinId, List<Long> paymentMethodIds) {
        Medecin medecin = medecinRepository.findById(medecinId)
                .orElseThrow(() -> new RuntimeException("Médecin introuvable"));

        List<MoyenPaiement> selectedMethods = moyenPaiementRepository.findAllById(paymentMethodIds);

        medecin.setMoyenPaiement(selectedMethods);
        return medecinRepository.save(medecin);
    }

    public List<MoyenPaiement> getMedecinPaymentMethods(Long medecinId) {
        Medecin medecin = medecinRepository.findById(medecinId)
                .orElseThrow(() -> new RuntimeException("Médecin introuvable"));
        return medecin.getMoyenPaiement();
    }

    public void disableMethod(Long id) {
        MoyenPaiement method = moyenPaiementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Méthode de paiement introuvable"));
        method.setEnabled(false);
        moyenPaiementRepository.save(method);
    }
}
