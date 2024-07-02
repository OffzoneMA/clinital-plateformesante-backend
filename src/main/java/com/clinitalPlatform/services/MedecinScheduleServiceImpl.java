package com.clinitalPlatform.services;

import com.clinitalPlatform.exception.BadRequestException;
import com.clinitalPlatform.exception.ConflictException;
import com.clinitalPlatform.models.*;
import com.clinitalPlatform.payload.request.MedecinScheduleRequest;
import com.clinitalPlatform.repository.CabinetRepository;
import com.clinitalPlatform.repository.MedecinScheduleRepository;
import com.clinitalPlatform.repository.ModeConsultationRepository;
import com.clinitalPlatform.services.interfaces.MedecinScheduleService;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;


@Transactional
@Service
public class MedecinScheduleServiceImpl implements MedecinScheduleService {


    @Autowired
    private MedecinScheduleRepository medecinScheduleRepository;

   @Autowired
    private ModeConsultationRepository moderepo;

    @Autowired
    private ClinitalModelMapper modelMapper;
//    @Autowired
//    private TypeConsultationServicesImpli typeConsult;
    @Autowired
    private MedecinServiceImpl medecinservices;
   @Autowired
   private CabinetRepository cabinetrepo;

    @PersistenceContext
    private EntityManager entityManger;

    @Autowired
    private GlobalVariables globalVariables;

    @Autowired
    private ActivityServices activityServices;

    private final Logger LOGGER= LoggerFactory.getLogger(getClass());

    // Creating a new instance of MedecinSchedule.
//    @Override
//    public MedecinSchedule create(MedecinScheduleRequest medecinScheduleRequest, Long id) throws Exception {
//        // Create a new instance of MedecinSchedule
//        MedecinSchedule schedule = new MedecinSchedule();
//        Medecin Med = medecinservices.getMedecinByUserId(id);
//       Cabinet cabinet=cabinetrepo.findById(medecinScheduleRequest.getCabinet_id())
//                .orElseThrow(()->new BadRequestException("No matching found for this Cabinet"));
//        // gerer chevauchement
//       // si la date start est compris entre un ancien creno ou commece par lui ou la date end aussi : exception
//        // ajouter le code ici
//        // Set attribut of MedecinSchedule
//        schedule.setDay(DayOfWeek.valueOf(medecinScheduleRequest.getDay()));
//        schedule.setAvailabilityStart(medecinScheduleRequest.getAvailabilityStart());
//        schedule.setAvailabilityEnd(medecinScheduleRequest.getAvailabilityEnd());
//        schedule.setModeconsultation(medecinScheduleRequest.getModeconsultation());
//        schedule.setMotifConsultation(medecinScheduleRequest.getMotifconsultation());
//        schedule.setPeriod(medecinScheduleRequest.getPeriod());
//        schedule.setMedecin(Med);
//        schedule.setCabinet(cabinet);
//        schedule.setIsnewpatient(medecinScheduleRequest.getIsnewpatient());
//         medecinScheduleRepository.save(schedule);
//        // Save it
//        //entityManger.persist(schedule);
//        activityServices.createActivity(new Date(),"Add","Create New Schedule ID :"+schedule.getId(),globalVariables.getConnectedUser());
//        LOGGER.info("Create New Schedule ID :"+schedule.getId()+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
//        return modelMapper.map(schedule, MedecinSchedule.class);
//    }
    public MedecinSchedule create(MedecinScheduleRequest medecinScheduleRequest, Long id) throws Exception {
        Medecin Med = medecinservices.getMedecinByUserId(id);
        Cabinet cabinet = cabinetrepo.findById(medecinScheduleRequest.getCabinet_id())
                .orElseThrow(() -> new BadRequestException("No matching found for this Cabinet"));

        // Vérifier les conflits
       List<MedecinSchedule> existingSchedules = medecinScheduleRepository.findByMedIdAndDay(Med.getId() , DayOfWeek.valueOf(medecinScheduleRequest.getDay()).getValue()-1);
        if(existingSchedules!=null){
            for (MedecinSchedule existingSchedule : existingSchedules) {
                if (isConflict(existingSchedule, medecinScheduleRequest)) {
                    throw new ConflictException("Ce créneau entre en conflit avec un créneau existant.");
                }
            }
        }

        // Vérifier si l'heure de fin est après l'heure de début
        if (medecinScheduleRequest.getAvailabilityEnd().isBefore(medecinScheduleRequest.getAvailabilityStart())) {
            throw new BadRequestException("L'heure de fin doit être après l'heure de début.");
        }


        // Créer un nouveau planning
        MedecinSchedule schedule = new MedecinSchedule();
        schedule.setDay(DayOfWeek.valueOf(medecinScheduleRequest.getDay()));
        schedule.setAvailabilityStart(medecinScheduleRequest.getAvailabilityStart());
        schedule.setAvailabilityEnd(medecinScheduleRequest.getAvailabilityEnd());
        schedule.setModeconsultation(medecinScheduleRequest.getModeconsultation());
        schedule.setMotifConsultation(medecinScheduleRequest.getMotifconsultation());
        schedule.setPeriod(medecinScheduleRequest.getPeriod());
        schedule.setMedecin(Med);
        schedule.setCabinet(cabinet);
        schedule.setIsnewpatient(medecinScheduleRequest.getIsnewpatient());

        // Enregistrer le nouveau planning
        medecinScheduleRepository.save(schedule);
        activityServices.createActivity(new Date(), "Add", "Create New Schedule ID :" + schedule.getId(), globalVariables.getConnectedUser());
        LOGGER.info("Create New Schedule ID:" + schedule.getId() + ", UserID:" + (globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId() : ""));
        return modelMapper.map(schedule, MedecinSchedule.class);
    }


    private boolean isConflict(MedecinSchedule existingSchedule, MedecinScheduleRequest newScheduleRequest) {
        LocalDateTime newStart = newScheduleRequest.getAvailabilityStart();
        LocalDateTime newEnd = newScheduleRequest.getAvailabilityEnd();

        LocalDateTime existingStart = existingSchedule.getAvailabilityStart();
        LocalDateTime existingEnd = existingSchedule.getAvailabilityEnd();

        // Extraire les heures et minutes des dates
        LocalTime newStartTime = newStart.toLocalTime();
        LocalTime newEndTime = newEnd.toLocalTime();
        LocalTime existingStartTime = existingStart.toLocalTime();
        LocalTime existingEndTime = existingEnd.toLocalTime();

        // Vérifier si les plages horaires se chevauchent

        if (newStartTime.compareTo(existingStartTime)==0 || newEndTime.compareTo(existingEndTime)==0) {
            // Les plages horaires sont identiques
            return true;
        }

        if (newStartTime.isAfter(existingStartTime) && newStartTime.isBefore(existingEndTime)) {
            // Le nouveau rendez-vous commence pendant une période déjà réservée
            return true;
        }

        //faites attention au milliseconde peut causer conflits
        if (newEndTime.isAfter(existingStartTime) && newEndTime.isBefore(existingEndTime) ) {
                // Le nouveau rendez-vous se termine pendant une période déjà réservée
                return true;
        }

        if (newStartTime.isBefore(existingStartTime) && newEndTime.isAfter(existingEndTime)) {
            // Le nouveau rendez-vous englobe complètement une période déjà réservée
            return true;
        }

        // Pas de conflit
        return false;
    }




    // Updating the MedecinSchedule.
    @Override
    public MedecinSchedule update(MedecinScheduleRequest req, Long id) throws Exception {
        MedecinSchedule isSchedule=medecinScheduleRepository.getById(id);
        if(isSchedule!=null){

            Medecin Med = medecinservices.findById(req.getMedecin_id());
            MedecinSchedule schedule = medecinScheduleRepository.getById(id);
            Cabinet cabinet=cabinetrepo.findById(req.getCabinet_id())
                  .orElseThrow(()->new BadRequestException("No matching found for this Cabinet"));

            // Set attribut of MedecinSchedule
            schedule.setDay(DayOfWeek.valueOf(req.getDay()));
            schedule.setAvailabilityStart(req.getAvailabilityStart());
            schedule.setAvailabilityEnd(req.getAvailabilityEnd());
            schedule.setPeriod(req.getPeriod());
            schedule.setIsnewpatient(req.getIsnewpatient());
            //motifs and modes
            schedule.setModeconsultation(req.getModeconsultation());
            schedule.setMotifConsultation(req.getMotifconsultation());

            schedule.setMedecin(Med);
            schedule.setCabinet(cabinet);

            medecinScheduleRepository.save(schedule);

            activityServices.createActivity(new Date(),"Update","Update New Schedule ID :"+schedule.getId(),globalVariables.getConnectedUser());
            LOGGER.info("Update Schedule ID :"+schedule.getId()+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));

            return schedule;

        } else{
            activityServices.createActivity(new Date(),"Error","Cant Update Schedule",globalVariables.getConnectedUser());
        LOGGER.error("Error to Update Schedule , UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
        throw new Exception("Failed to update");}
    }

    @Override
    public void deleteById(Long id) throws Exception {
        MedecinSchedule medecinSchedule = medecinScheduleRepository.getById(id);
        if (medecinSchedule!=null) {
            medecinScheduleRepository.deleteById(id);
            activityServices.createActivity(new Date(),"Delete","Delete Schedule ID :"+id,globalVariables.getConnectedUser());
            LOGGER.info("Delete Schedule ID :"+id+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
        } else {
            activityServices.createActivity(new Date(), "Warning", "Cant Delete Schedule ID :" + id, globalVariables.getConnectedUser());
            LOGGER.warn("Cant Delete Schedule ID :" + id + ", UserID : " + (globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId() : ""));
            throw new Exception("Fail to delete");
        }
    }

    // A method that returns a list of MedecinSchedule objects by id Medecin.
    public List<MedecinSchedule> getAllSchedulesByMedId(long idmed) throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            // L'utilisateur est authentifié, créez l'activité
            User connectedUser = globalVariables.getConnectedUser();
            if (connectedUser != null) {
                activityServices.createActivity(new Date(),"Read","Consulting All Schedules",globalVariables.getConnectedUser());
                LOGGER.info("Consulting All Schedules , UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));

            }
        }
                return medecinScheduleRepository.getAllSchedulesByMedId(idmed)
                .stream()
                .map(schedule -> modelMapper.map(schedule, MedecinSchedule.class))
                .collect(Collectors.toList());

    }

    // Returning a single `MedecinSchedule` object.
    public MedecinSchedule GetAllSchedulesByIdsched(long idsched) throws Exception {
        MedecinSchedule medecinSchedule=medecinScheduleRepository.findById(idsched).orElseThrow(()->new Exception("No Matching Found"));
        activityServices.createActivity(new Date(),"Read","Consulting Schedule by id :"+idsched,globalVariables.getConnectedUser());
        LOGGER.info("Consulting Schedule by id :"+idsched+" , UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
        return medecinSchedule;

    }

    // A method that returns a list of MedecinSchedule objects by id Medecin and id TypeConsultation.
    public List<MedecinSchedule> getAllSchedulesByMedIdandIdConsult(long idmed, long idconsult) throws Exception {

        activityServices.createActivity(new Date(),"Read","Consulting Schedule by id Med :"+idmed+" and ID Consultation : "+idconsult,globalVariables.getConnectedUser());
        LOGGER.info("Consulting Schedule by id Med :"+idmed+" and ID Consultation : "+idconsult+" , UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
        return medecinScheduleRepository.getAllSchedulesByMedIdandIdConsult(idmed, idconsult)
                .stream()
                .map(schedule -> modelMapper.map(schedule, MedecinSchedule.class))
                .collect(Collectors.toList());

    }


    public MedecinSchedule getScheduleFromCreno(LocalTime creno, DayOfWeek day, long idmed) throws Exception {
        // Récupérer la liste des plannings du médecin
        List<MedecinSchedule> schedules = this.getAllSchedulesByMedId(idmed);

        // Parcourir chaque planning
        for (MedecinSchedule schedule : schedules) {
            // Vérifier si le jour correspond au jour du planning
            if (schedule.getDay().equals(day)) {
                // Vérifier si le créneau se situe entre l'heure de début et de fin du planning
                LocalTime start = schedule.getAvailabilityStart().toLocalTime();
                LocalTime end = schedule.getAvailabilityEnd().toLocalTime();
                if (creno.equals(start) || (creno.isAfter(start) && creno.isBefore(end))) {
                    // Le créneau appartient à ce planning, retourner ce planning
                    return schedule;
                }
            }
        }

        // Aucun planning trouvé pour ce créneau
        return null;
    }



    //FILTRE SUR LES HORAIRES-----------------------------------------------

    //Filtre de crenneau selon la disponobilité
    public Map<Long, List<MedecinSchedule>> filterSchedulesByAvailability(List<Long> medecinIds, String filter) {
        return medecinIds.stream()
                .collect(Collectors.toMap(
                        medecinId -> medecinId,
                        medecinId -> filterSchedulesByMedecinAndAvailability(medecinId, filter)
                ));
    }

    private List<MedecinSchedule> filterSchedulesByMedecinAndAvailability(Long medecinId, String filter) {
        List<MedecinSchedule> schedules = medecinScheduleRepository.findByMedId(medecinId);

        switch (filter) {
            case "nextTwoDays":
                return filterSchedulesForNextTwoDays(schedules);
            case "weekend":
                return filterSchedulesForWeekend(schedules);
            case "weekday":
                return filterSchedulesForWeekday(schedules);
            default:
                return schedules;
        }
    }

    private List<MedecinSchedule> filterSchedulesForNextTwoDays(List<MedecinSchedule> schedules) {
        LocalDate now = LocalDate.now();
        LocalDate twoDaysLater = now.plusDays(2);
        return schedules.stream()
                .filter(schedule -> schedule.getAvailabilityStart().toLocalDate().isAfter(now) &&
                        schedule.getAvailabilityStart().toLocalDate().isBefore(twoDaysLater))
                .collect(Collectors.toList());
    }

    private List<MedecinSchedule> filterSchedulesForWeekend(List<MedecinSchedule> schedules) {
        return schedules.stream()
                .filter(schedule -> schedule.getDay() == DayOfWeek.SATURDAY || schedule.getDay() == DayOfWeek.SUNDAY)
                .collect(Collectors.toList());
    }

    private List<MedecinSchedule> filterSchedulesForWeekday(List<MedecinSchedule> schedules) {
        return schedules.stream()
                .filter(schedule -> schedule.getDay() != DayOfWeek.SATURDAY && schedule.getDay() != DayOfWeek.SUNDAY)
                .collect(Collectors.toList());
    }


    //FILTRE DE CRENEAU PAR  DISPONIBILITÉ DES MEDECINS------------------------

    public List<Medecin> filterMedecinsByAvailability(List<Long> medecinIds, String filter) {
        // Récupérez les plannings filtrés
        Map<Long, List<MedecinSchedule>> filteredSchedules = filterSchedulesByAvailability(medecinIds, filter);

        // Initialisez une liste pour stocker les médecins filtrés
        List<Medecin> filteredMedecins = new ArrayList<>();

        // Parcourez chaque liste de plannings dans la carte filtrée
        for (List<MedecinSchedule> schedules : filteredSchedules.values()) {
            // Parcourez chaque planning dans la liste
            for (MedecinSchedule schedule : schedules) {
                // Vérifiez si le médecin associé n'est pas déjà présent dans la liste
                if (!filteredMedecins.contains(schedule.getMedecin())) {
                    // Ajoutez le médecin à la liste filtrée
                    filteredMedecins.add(schedule.getMedecin());
                }
            }
        }

        // Retournez la liste des médecins filtrés
        return filteredMedecins;
    }
//FILTRE SELON LE MOTIF DE CONSULTATION

    //Filtrer les crennaux selon la disponibilité
   /* private List<MedecinSchedule> filterSchedulesByMotif(List<MedecinSchedule> schedules, String motif) {
        return schedules.stream()
                .filter(schedule -> schedule.getMotifConsultation().equals(motif))
                .collect(Collectors.toList());
    }*/





}

