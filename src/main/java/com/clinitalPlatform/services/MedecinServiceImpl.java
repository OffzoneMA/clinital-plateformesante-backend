package com.clinitalPlatform.services;


import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import com.clinitalPlatform.dto.MedecinDTO;
import com.clinitalPlatform.enums.MotifConsultationEnum;
import com.clinitalPlatform.enums.RdvStatutEnum;
import com.clinitalPlatform.services.interfaces.MedecinScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.repository.MedecinRepository;
import com.clinitalPlatform.services.interfaces.MedecinService;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;


import com.clinitalPlatform.models.*;
import com.clinitalPlatform.payload.response.AgendaResponse;
import com.clinitalPlatform.payload.response.RendezvousResponse;
import com.clinitalPlatform.repository.*;
import org.springframework.web.multipart.MultipartFile;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import java.util.stream.Collectors;

@Transactional
@Service
public class MedecinServiceImpl implements MedecinService {


    @Autowired
    private MedecinRepository medecinRepository;

    @Autowired
    private ClinitalModelMapper clinitalModelMapper;

    @Autowired
    private ActivityServices activityServices;

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private DemandeServiceImpl demandeServiceImpl;

    @Autowired
    private GlobalVariables globalVariables;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private RendezvousService rendezvousService;

    @Autowired
    private MedecinScheduleRepository medecinScheduleRepository;
    @Autowired
    private MotifConsultationRepository motifConsultationRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    private final Logger LOGGER=LoggerFactory.getLogger(getClass());

    @Override
        public Medecin findById(Long id) throws Exception {

        Medecin med = medecinRepository.findById(id).orElseThrow(() -> new Exception("Medecin not found"));

        return clinitalModelMapper.map(med, Medecin.class);
    }

    @Override
    public Medecin getMedecinByUserId(long id) throws Exception {

        Medecin med = medecinRepository.getMedecinByUserId(id);

        return clinitalModelMapper.map(med, Medecin.class);
    }

    public Medecin updateMedecin (Long id , MedecinDTO medecinDTO) {
        Medecin medecin = medecinRepository.findById(id).orElseThrow(() -> new RuntimeException("Médecin introuvable"));
        if(medecinDTO.getNom_med() != null && !medecinDTO.getNom_med().isEmpty()) {
            medecin.setNom_med(medecinDTO.getNom_med());
        }
        if(medecinDTO.getPrenom_med() != null && !medecinDTO.getPrenom_med().isEmpty()) {
            medecin.setPrenom_med(medecinDTO.getPrenom_med());
        }
        if(medecinDTO.getContact_urgence_med() != null && !medecinDTO.getContact_urgence_med().isEmpty()) {
            medecin.setContact_urgence_med(medecinDTO.getContact_urgence_med());
        }
        if(medecinDTO.getDescription_med() != null && !medecinDTO.getDescription_med().isEmpty()) {
            medecin.setDescription_med(medecinDTO.getDescription_med());
        }

        return medecinRepository.save(medecin);

    }

    // Creating a creno.
    public AgendaResponse CreateCreno(MedecinSchedule Medsch, AgendaResponse agenda, long idmed, long week,
                                      LocalDateTime date) {

        long minutes = ChronoUnit.MINUTES.between(Medsch.getAvailabilityStart(),
                Medsch.getAvailabilityEnd());

        long totalSlots = minutes / Medsch.getPeriod().getValue();

        LocalDateTime timer = Medsch.getAvailabilityStart();

        agenda.setDay(Medsch.getDay());
        agenda.setWorkingDate(date);
        agenda.setWeek(week);
        agenda.setPeriod(Medsch.getPeriod());
        agenda.setIsnewpatient(Medsch.getIsnewpatient());
        agenda.setMotifconsultation(Medsch.getMotifConsultation());
        agenda.setModeconsultation(Medsch.getModeconsultation());
        List<Rendezvous> rendezvous = rendezvousService.findRendezvousByMedAndDate(idmed, date);
        List<RendezvousResponse> rdvrespo = rendezvous.stream()
                .map(rdv -> clinitalModelMapper.map(rdv, RendezvousResponse.class)).collect(Collectors.toList());

        //new
        for (int j = 0; j < totalSlots; j++) {
            boolean isReserved = false;
            LocalDateTime slotTime = timer; // Créneau actuel

                for (RendezvousResponse rdv : rdvrespo) {
                    //rdv.getStart().isEqual(slotTime)
                    LocalDateTime rdvStart = rdv.getStart();
                    LocalDateTime rdvEnd = rdv.getEnd();
                    // Vérifier si le créneau actuel se situe entre l'heure de début et de fin du rendez-vous
//                    if ((slotTime.isEqual(rdvStart) || (slotTime.isAfter(rdvStart) && slotTime.isBefore(rdvEnd)))
//                            && rdvStart.toLocalDate().isEqual(date.toLocalDate()))
//                    {
//                        LOGGER.info("isReserved ");

//                        isReserved = true;
//                        break;
//                    }
                    if(rdv.getStatut().equals(RdvStatutEnum.ANNULE)){
                        isReserved=false;
                        continue;
                    }
                    //cas conge longue duree
                    if(rdv.getStatut().equals(RdvStatutEnum.CONJE)) {
                        if (rdvStart.toLocalDate().isBefore(date.toLocalDate()) && rdvEnd.toLocalDate().isAfter(date.toLocalDate()) || rdvStart.toLocalDate().isEqual(date.toLocalDate()) || rdvEnd.toLocalDate().isEqual(date.toLocalDate())) {
                            isReserved = true;
                            break;
                        }
                    }
                    if ((slotTime.getHour() == rdvStart.getHour() && slotTime.getMinute() == rdvStart.getMinute())
                            || (slotTime.toLocalTime().isAfter(rdvStart.toLocalTime()) && slotTime.toLocalTime().isBefore(rdvEnd.toLocalTime()))
                            && date.toLocalDate().isEqual(rdvStart.toLocalDate()) ) {
                        isReserved = true;
                        break;
                    }

//
                }

            if (!isReserved) {
                agenda.getAvailableSlot().add((timer.getHour() < 10 ? "0" : "") + timer.getHour() + ":" +
                        (timer.getMinute() < 10 ? "0" : "") + timer.getMinute());
            }

            timer = timer.plusMinutes(Medsch.getPeriod().getValue());
        }
        //end new

        return agenda;

    }

    // It's returning the number of days in a month.
    public int getDaysInMonth(LocalDateTime localDateTime) {

        int daysInMonth = 0;
        int year = localDateTime.getYear();
        int month = localDateTime.getMonth().getValue();

        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                daysInMonth = 31;
                break;
            case 4:
            case 6:
            case 9:
            case 11:
                daysInMonth = 30;
                break;
            case 2:
                if (((year % 4 == 0) && !(year % 100 == 0) || (year % 400 == 0))) {
                    daysInMonth = 29;
                } else {
                    daysInMonth = 28;
                }
                break;
            default:
                System.out.println("Invalid month");
                break;
        }

        return daysInMonth;
    }

    //Recuperer les langues parlées par les médecins par medecinId
    @Override
    public List<Langue> getLanguesByMedecinId(Long medecinId) throws Exception {
        // Rechercher le médecin par son ID
        Medecin medecin = medecinRepository.findById(medecinId)
                .orElseThrow(() -> new Exception("Médecin non trouvé pour l'ID: " + medecinId));

        // Récupérer les langues associées à ce médecin
        return medecin.getLangues();
    }

    //Recuperer les langues parlées par les médecins par le nom du medecin

    @Override
    public List<Langue> getLanguesByMedecinName(String nomMed) throws Exception {
        Optional<Medecin> optionalMedecin = medecinRepository.findMedecinByName(nomMed);

        if (optionalMedecin.isEmpty()) {
            throw new Exception("Aucun médecin trouvé pour le nom: " + nomMed);
        }

        Medecin medecin = optionalMedecin.get();
        return medecin.getLangues();
    }

    @Override
    public List<Tarif> getTarifByMedecinId(Long medecinId) throws Exception {
        Medecin medecin = medecinRepository.findById(medecinId)
                .orElseThrow(() -> new Exception("Médecin non trouvé pour l'ID: " + medecinId));


        return medecin.getTarifs();
    }

    @Override
    public List<Tarif> getTarifByMedecinName(String nomMed) throws Exception {
        Optional<Medecin> optionalMedecin = medecinRepository.findMedecinByName(nomMed);

        if (optionalMedecin.isEmpty()) {
            throw new Exception("Aucun médecin trouvé pour le nom: " + nomMed);
        }

        Medecin medecin = optionalMedecin.get();
        return medecin.getTarifs();
    }

    @Override
    public List<Medecin> findMedecinsByLangues_Name(String langueName) {
        return medecinRepository.findMedecinsByLangues_Name(langueName);

    }

   public List<Medecin> filterMedecinsByLangue(List<Long> medecinIds, List<String> langueNames) {
       // Récupérez les médecins correspondant aux IDs fournis
       List<Medecin> medecins = medecinRepository.findAllById(medecinIds);

       // Filtrer les médecins par langues
       List<Medecin> filteredMedecins = medecins.stream()
               .filter(medecin -> medecin.getLangues().stream()
                       .anyMatch(langue -> langueNames.contains(langue.getName())))
               .collect(Collectors.toList());

       return filteredMedecins;
   }

    //RECHERCHE DE MEDECIN PAR MOTIF

    public List<Medecin> getMedecinsByMotif(List<String> libelles, List<Long> medecinIds) {
        // Récupérer les IDs des motifs à partir des libellés
        List<Long> motifIds = motifConsultationRepository.findIdsByLibelles(libelles);

        // Trouver les médecins dont les schedules ont les IDs de motifs recherchés
        List<MedecinSchedule> schedules = medecinScheduleRepository.findByMotifConsultationIdIn(motifIds);

        // Extraire les IDs des médecins à partir des schedules
        Set<Long> filteredMedecinIds = schedules.stream()
                .map(schedule -> schedule.getMedecin().getId())
                .collect(Collectors.toSet());

        // Filtrer les médecins par les IDs donnés
        Set<Long> finalMedecinIds = filteredMedecinIds.stream()
                .filter(medecinIds::contains)
                .collect(Collectors.toSet());

        // Récupérer les médecins par leurs IDs filtrés
        return medecinRepository.findAllById(finalMedecinIds);
    }

    //FILTRE COMBINÉ

   /* public List<Medecin> filterMedecins(List<Long> medecinIds, List<String> langueNames, List<String> libellesMotifs, List<String> availabilityFilters) {

        // 1. Filtrer par langue
        List<Medecin> filteredByLangue = filterMedecinsByLangue(medecinIds, langueNames);

        // 2. Filtrer par motif sur les résultats filtrés par langue
        List<Medecin> filteredByMotif = getMedecinsByMotif(libellesMotifs, filteredByLangue.stream()
                .map(Medecin::getId)
                .collect(Collectors.toList()));

        // 3. Filtrer par disponibilité sur les résultats filtrés par langue et motif
        List<Medecin> filteredByAvailability = medecinScheduleService.filterMedecinsByAvailability(filteredByMotif.stream()
                .map(Medecin::getId)
                .collect(Collectors.toList()), availabilityFilters);

        // 4. Retourner les résultats finaux
        return filteredByAvailability;
    }
*/
    public Medecin updatePhotoProfil(Long idMedecin, MultipartFile file) throws IOException {
        Medecin medecin = medecinRepository.findById(idMedecin)
                .orElseThrow(() -> new RuntimeException("Médecin introuvable"));

        //Suppression de l'ancienne image
        if (medecin.getPhoto_med() != null) {
            cloudinaryService.deleteImage(medecin.getPhoto_med());
        }

        //Téléchargement de la nouvelle image
        String imageUrl = cloudinaryService.uploadImage(file, "medecins/photo_profil");
        medecin.setPhoto_med(imageUrl);
        return medecinRepository.save(medecin);
    }

    public Medecin updatePhotoCouverture(Long idMedecin, MultipartFile file) throws IOException {
        Medecin medecin = medecinRepository.findById(idMedecin)
                .orElseThrow(() -> new RuntimeException("Médecin introuvable"));

        //Suppression de l'ancienne image
        if (medecin.getPhoto_couverture_med() != null) {
            cloudinaryService.deleteImage(medecin.getPhoto_couverture_med());
        }

        //Téléchargement de la nouvelle image
        String imageUrl = cloudinaryService.uploadImage(file, "medecins/photo_couverture");
        medecin.setPhoto_couverture_med(imageUrl);
        return medecinRepository.save(medecin);
    }

    public Medecin deletePhotoProfil(Long idMedecin) throws IOException {
        Medecin medecin = medecinRepository.findById(idMedecin)
                .orElseThrow(() -> new RuntimeException("Médecin introuvable"));

        if (medecin.getPhoto_med() != null) {
            cloudinaryService.deleteImage(medecin.getPhoto_med());
            medecin.setPhoto_med(null);
        }
        return medecinRepository.save(medecin);
    }

    public Medecin deletePhotoCouverture(Long idMedecin) throws IOException {
        Medecin medecin = medecinRepository.findById(idMedecin)
                .orElseThrow(() -> new RuntimeException("Médecin introuvable"));

        if (medecin.getPhoto_couverture_med() != null) {
            cloudinaryService.deleteImage(medecin.getPhoto_couverture_med());
            medecin.setPhoto_couverture_med(null);
        }
        return medecinRepository.save(medecin);
    }

}

