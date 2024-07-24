package com.clinitalPlatform.services;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import com.clinitalPlatform.enums.RdvStatutEnum;
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
        ActivityServices activityServices;


    @Autowired
        EmailSenderService emailSenderService;

    @Autowired
        DemandeServiceImpl demandeServiceImpl;

    @Autowired
        GlobalVariables globalVariables;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    RendezvousService rendezvousService;

    @Autowired
    MedecinScheduleRepository medecinScheduleRepository;




    private final Logger LOGGER=LoggerFactory.getLogger(getClass());
    @Autowired
    private RdvRepository rdvRepository;

    @Override
public Medecin findById(Long id) throws Exception {

        Medecin med = medecinRepository.findById(id).orElseThrow(() -> new Exception("Medecin not found"));

        return clinitalModelMapper.map(med, Medecin.class);
        }

@Override
public Medecin getMedecinByUserId(long id) throws Exception {

        Medecin med = medecinRepository.getMedecinByUserId(id);
LOGGER.info(med.toString());
        return clinitalModelMapper.map(med, Medecin.class);
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

    public List<Medecin> filterMedecinsByLangue(List<Long> medecinIds, String langueName) {
        // Récupérez les médecins correspondant aux IDs fournis
        List<Medecin> medecins = medecinRepository.findAllById(medecinIds);

        // Filtrer les médecins par langue
        List<Medecin> filteredMedecins = medecins.stream()
                .filter(medecin -> medecin.getLangues().stream()
                        .anyMatch(langue -> langue.getName().equals(langueName)))
                .collect(Collectors.toList());

        return filteredMedecins;
    }


    public List<Patient> findPatientsByMedecinId(Long medecinId) {
        List<Long> l=medecinRepository.findPatientIdsByMedecinId(medecinId);
        List<Patient> patients = new ArrayList<>();
        for (Long id : l) {
            patientRepository.findById(id).ifPresent(patients::add);
        }

        return patients ;
    }
    public List<Rendezvous> getAllRdvsforpatient() throws Exception {

        Medecin medecin = medecinRepository.getMedecinByUserId(globalVariables.getConnectedUser().getId());
        List<Long> patientIds = medecinRepository.findPatientIdsByMedecinId(medecin.getId());

        List<Rendezvous> rdvs = new ArrayList<>();
        if (!patientIds.isEmpty()) {
            List<Long> rdvIds = medecinRepository.findRdvIdsByPatientIds(patientIds);
            rdvs = rdvRepository.findAllById(rdvIds);
        }

        LOGGER.info("Show All Rdvs for Medecin, UserID : " + globalVariables.getConnectedUser().getId());

        return rdvs;
    }

    public Long getDistinctMedecinsCountByPatientId(Long patientId) {
        return medecinRepository.NumberMedecinsByPatientId(patientId);
    }


}

