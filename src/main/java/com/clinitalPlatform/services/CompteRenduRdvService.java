package com.clinitalPlatform.services;

import com.clinitalPlatform.models.CompteRenduRdv;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.Patient;
import com.clinitalPlatform.models.Rendezvous;
import com.clinitalPlatform.payload.request.CompteRenduRdvRequest;
import com.clinitalPlatform.repository.CompteRenduRdvRepository;
import com.clinitalPlatform.repository.MedecinRepository;
import com.clinitalPlatform.repository.PatientRepository;
import com.clinitalPlatform.repository.RdvRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class CompteRenduRdvService {

    @Autowired
    private CompteRenduRdvRepository compteRenduRdvRepository;

    @Autowired
    private MedecinRepository medecinRepository;

    @Autowired
    private RdvRepository rdvRepository;

    @Autowired
    private PatientRepository patientRepository;

    private static final SecureRandom secureRandom = new SecureRandom();

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    public List<CompteRenduRdv> findAll() {
        return compteRenduRdvRepository.findAll();
    }

    public Optional<CompteRenduRdv> findById(Long id) {
        return compteRenduRdvRepository.findById(id);
    }

    public CompteRenduRdv createCompteRendu(CompteRenduRdvRequest request, Medecin medecin) {
        Rendezvous rdv = rdvRepository.findById(request.getRdvId())
                .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé"));

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient non trouvé"));

        CompteRenduRdv cr = new CompteRenduRdv();
        cr.setContenu(request.getContenu());
        cr.setRdv(rdv);
        cr.setMedecin(medecin);
        cr.setPatient(patient);
        cr.setDate_ajout(LocalDateTime.now());

        String numero = generateNumero();
        cr.setNumero(numero);
        String fileNameNo = generateNumero();
        cr.setNom_fichier("compte_rendu_" + fileNameNo + ".pdf");

        return compteRenduRdvRepository.save(cr);
    }

    public CompteRenduRdv update(CompteRenduRdvRequest compteRendu , Long id) {
        // Vérifier si le compte rendu existe
        Optional<CompteRenduRdv> existingCompteRendu = compteRenduRdvRepository.findById(id);
        if (existingCompteRendu.isPresent()) {
            CompteRenduRdv updatedCompteRendu = existingCompteRendu.get();
            updatedCompteRendu.setContenu(compteRendu.getContenu());
            return compteRenduRdvRepository.save(updatedCompteRendu);
        } else {
            return null;
        }
    }

    public void deleteById(Long id) {
        compteRenduRdvRepository.deleteById(id);
    }

    public List<CompteRenduRdv> findByPatientId(Long patientId) {
        return compteRenduRdvRepository.findByPatientId(patientId);
    }

    public List<CompteRenduRdv> findByMedecinId(Long medecinId) {
        return compteRenduRdvRepository.findByMedecinId(medecinId);
    }

    public List<CompteRenduRdv> findByRendezvousId(Long rdvId) {
        return compteRenduRdvRepository.findByRendezvousId(rdvId);
    }

    private String generateRandomCode(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Sans lettres ambiguës : O, I, 0, 1
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    private String generateNumero() {
        String timestamp = LocalDateTime.now().format(formatter); // "yyyyMMdd-HHmmss"
        String randomCode = generateRandomCode(6); // Exemple : "7K9XG2"
        return "CRD-" + timestamp + "-" + randomCode;
    }



}

