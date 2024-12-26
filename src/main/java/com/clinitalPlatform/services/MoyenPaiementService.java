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

    // Affecter un moyen de paiement à un médecin avec des informations de virement bancaire
    public Medecin affecterMoyenPaiement(Long medecinId, Long moyenPaiementId,
                                         VirementBancaireDTO virementBancaireDTO) {
        Medecin medecin = medecinRepository.findById(medecinId)
                .orElseThrow(() -> new IllegalArgumentException("Médecin non trouvé avec l'ID : " + medecinId));

        MoyenPaiement moyenPaiement = moyenPaiementRepository.findById(moyenPaiementId)
                .orElseThrow(() -> new IllegalArgumentException("Moyen de paiement non trouvé avec l'ID : " + moyenPaiementId));

        if (moyenPaiement.getType() == TypeMoyenPaiementEnum.Virement) {
            VirementBancaire virementBancaire = new VirementBancaire(virementBancaireDTO.getRib(),
                    virementBancaireDTO.getCodeSwift(), virementBancaireDTO.getBankName());
            virementBancaire.setMedecin(medecin);
            virementBancaire.setMoyenPaiement(moyenPaiement);
            virementBancaireRepository.save(virementBancaire);
        }

        medecin.getMoyenPaiement().add(moyenPaiement);
        Medecin savedMedecin = medecinRepository.save(medecin);
        return savedMedecin;
    }

    // Récupérer les moyens de paiement pour un médecin
    public List<MoyenPaiementDTO> getMoyensPaiementPourMedecin(Long medecinId) {
        Medecin medecin = medecinRepository.findById(medecinId)
                .orElseThrow(() -> new IllegalArgumentException("Médecin non trouvé avec l'ID : " + medecinId));

        return medecin.getMoyenPaiement().stream()
                .map(moyenPaiement -> {
                    MoyenPaiementDTO moyenPaiementDTO = modelMapper.map(moyenPaiement, MoyenPaiementDTO.class);
                    if (moyenPaiement.getType() == TypeMoyenPaiementEnum.Virement) {
                        Optional<VirementBancaire> virementBancaire = virementBancaireRepository
                                .findByMedecinIdAndMoyenPaiementId_mp(medecinId, moyenPaiement.getId_mp());
                        virementBancaire.ifPresent(vb -> {
                            VirementBancaireDTO virementBancaireDTO = modelMapper.map(vb, VirementBancaireDTO.class);
                            moyenPaiementDTO.setVirementBancaire(virementBancaireDTO);
                        });
                    }
                    return moyenPaiementDTO;
                })
                .collect(Collectors.toList());
    }
}
