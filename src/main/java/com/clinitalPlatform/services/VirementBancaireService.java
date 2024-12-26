package com.clinitalPlatform.services;

import com.clinitalPlatform.dto.VirementBancaireDTO;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.VirementBancaire;
import com.clinitalPlatform.repository.MedecinRepository;
import com.clinitalPlatform.repository.VirementBancaireRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VirementBancaireService {

    @Autowired
    private VirementBancaireRepository virementBancaireRepository;

    @Autowired
    private MedecinRepository medecinRepository;

    @Autowired
    private ModelMapper modelMapper;

    // Ajouter un virement bancaire
    public VirementBancaireDTO createVirementBancaire(VirementBancaire virementBancaire) {
        VirementBancaire savedVirementBancaire = virementBancaireRepository.save(virementBancaire);
        return modelMapper.map(savedVirementBancaire, VirementBancaireDTO.class);
    }

    // Récupérer tous les virements bancaires
    public List<VirementBancaireDTO> getAllVirementsBancaires() {
        List<VirementBancaire> virementsBancaires = virementBancaireRepository.findAll();
        return virementsBancaires.stream()
                .map(virement -> modelMapper.map(virement, VirementBancaireDTO.class))
                .collect(Collectors.toList());
    }

    // Récupérer un virement bancaire par ID
    public Optional<VirementBancaireDTO> getVirementBancaireById(Long id) {
        Optional<VirementBancaire> virementBancaire = virementBancaireRepository.findById(id);
        return virementBancaire.map(virement -> modelMapper.map(virement, VirementBancaireDTO.class));
    }

    // Récupérer les détails des virements bancaires pour un médecin donné
    public List<VirementBancaireDTO> getDetailsVirementsPourMedecin(Long medecinId) {
        Medecin medecin = medecinRepository.findById(medecinId)
                .orElseThrow(() -> new IllegalArgumentException("Médecin non trouvé avec l'ID : " + medecinId));

        return medecin.getVirementsBancaires().stream()
                .map(virement -> modelMapper.map(virement, VirementBancaireDTO.class))
                .collect(Collectors.toList());
    }

    // Supprimer un virement bancaire
    public void deleteVirementBancaire(Long id) {
        if (!virementBancaireRepository.existsById(id)) {
            throw new IllegalArgumentException("Virement bancaire non trouvé avec l'ID : " + id);
        }
        virementBancaireRepository.deleteById(id);
    }
}
