package com.clinitalPlatform.services;

import com.clinitalPlatform.exception.BadRequestException;
import com.clinitalPlatform.exception.ConflictException;
import com.clinitalPlatform.models.*;
import com.clinitalPlatform.payload.request.MedecinMultiScheduleRequest;
import com.clinitalPlatform.payload.request.MedecinScheduleConfigRequest;
import com.clinitalPlatform.payload.request.MedecinScheduleRequest;
import com.clinitalPlatform.repository.CabinetRepository;
import com.clinitalPlatform.repository.MedecinScheduleRepository;
import com.clinitalPlatform.repository.ModeConsultationRepository;
import com.clinitalPlatform.services.interfaces.MedecinScheduleService;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import javassist.NotFoundException;
import org.hibernate.Hibernate;
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

    public List<MedecinSchedule> createMultiSchedule(MedecinMultiScheduleRequest multiScheduleRequest, Long userId) throws Exception {
        Medecin medecin = medecinservices.getMedecinByUserId(userId);
        Cabinet cabinet = cabinetrepo.findById(medecin.getFirstCabinetId())
                .orElseThrow(() -> new Exception("No Matching Cabinet found"));

        List<MedecinSchedule> createdSchedules = new ArrayList<>();

        // Process each available day
        for (DayOfWeek day : multiScheduleRequest.getAvailableDays()) {
            // Process each time slot for this day
            for (MedecinMultiScheduleRequest.TimeSlot slot : multiScheduleRequest.getTimeSlots()) {
                // Verify that end time is after start time
                if (slot.getEndTime().isBefore(slot.getStartTime())) {
                    throw new BadRequestException("L'heure de fin doit être après l'heure de début pour le créneau: "
                            + slot.getStartTime() + " - " + slot.getEndTime());
                }

                // Check for conflicts with existing schedules for this day
                List<MedecinSchedule> existingSchedules = medecinScheduleRepository.findByMedIdAndDay(medecin.getId(), day.getValue() - 1);
                if (existingSchedules != null) {
                    for (MedecinSchedule existingSchedule : existingSchedules) {
                        // Convert the request to a format compatible with the conflict check method
                        MedecinScheduleRequest tempRequest = new MedecinScheduleRequest();
                        tempRequest.setAvailabilityStart(slot.getStartTime());
                        tempRequest.setAvailabilityEnd(slot.getEndTime());
                        tempRequest.setDay(day.toString());

                        if (isConflict(existingSchedule, tempRequest)) {
                            throw new ConflictException("Ce créneau entre en conflit avec un créneau existant pour le jour: " + day);
                        }
                    }
                }

                // Create new schedule for this day and time slot
                MedecinSchedule schedule = new MedecinSchedule();
                schedule.setDay(day);
                schedule.setAvailabilityStart(slot.getStartTime());
                schedule.setAvailabilityEnd(slot.getEndTime());
                schedule.setModeconsultation(multiScheduleRequest.getModesConsultation());
                schedule.setMotifConsultation(multiScheduleRequest.getMotifsConsultation());

                // Set the consultation duration
                schedule.setPeriod(slot.getPeriod());

                schedule.setMedecin(medecin);
                schedule.setCabinet(cabinet);

                // Set patient categories
                schedule.setIsnewpatient(multiScheduleRequest.getAllowNewPatients());
                schedule.setIsFollowUpPatients(multiScheduleRequest.getAllowFollowUpPatients());

                // Save the schedule
                medecinScheduleRepository.save(schedule);
                createdSchedules.add(schedule);

                // Log activity
                activityServices.createActivity(new Date(), "Add", "Create New Schedule ID :" + schedule.getId(),
                        globalVariables.getConnectedUser());
                LOGGER.info("Create New Schedule ID:" + schedule.getId() +
                        ", UserID:" + (globalVariables.getConnectedUser() instanceof User ?
                        globalVariables.getConnectedUser().getId() : ""));
            }
        }

        return createdSchedules;
    }

    public List<MedecinSchedule> updateMultipliScheduleConfig(List<MedecinScheduleConfigRequest> requests , Long userId) throws Exception {
        Medecin medecin = medecinservices.getMedecinByUserId(userId);
        Cabinet cabinet = cabinetrepo.findById(medecin.getFirstCabinetId())
                .orElseThrow(() -> new Exception("No Matching Cabinet found"));

        List<MedecinSchedule> createdSchedules = new ArrayList<>();

        // Process each available day
        for (MedecinScheduleConfigRequest request : requests) {
            // Process each time slot for this day
            if(request.getId() != null && request.getId() != 0) {
                //Update existing schedule
                MedecinSchedule existingSchedule = medecinScheduleRepository.findById(request.getId())
                        .orElseThrow(() -> new NotFoundException("Aucun planning trouvé avec cet ID."));

                existingSchedule.setAvailabilityStart(request.getAvailabilityStart());
                existingSchedule.setAvailabilityEnd(request.getAvailabilityEnd());

                medecinScheduleRepository.save(existingSchedule);
                createdSchedules.add(existingSchedule);
            }
            else {
                // Create new schedule
                MedecinSchedule schedule = new MedecinSchedule();
                schedule.setDay(DayOfWeek.valueOf(request.getDay()));
                schedule.setAvailabilityStart(request.getAvailabilityStart());
                schedule.setAvailabilityEnd(request.getAvailabilityEnd());
                schedule.setModeconsultation(request.getModeconsultation());
                schedule.setMotifConsultation(request.getMotifconsultation());
                schedule.setPeriod(request.getPeriod());
                schedule.setMedecin(medecin);
                schedule.setCabinet(cabinet);
                schedule.setIsnewpatient(request.getAllowNewPatients());
                schedule.setIsFollowUpPatients(request.getAllowFollowUpPatients());

                medecinScheduleRepository.save(schedule);
                createdSchedules.add(schedule);

                activityServices.createActivity(new Date(), "Add", "Create New Schedule ID :" + schedule.getId(),
                        globalVariables.getConnectedUser());

                LOGGER.info("Create New Schedule ID:" + schedule.getId() +
                        ", UserID:" + (globalVariables.getConnectedUser() != null ?
                        globalVariables.getConnectedUser().getId() : ""));
            }
        }

        return createdSchedules;
    }

    public List<MedecinSchedule> createMultiScheduleForMedecin(MedecinMultiScheduleRequest request) throws Exception {
        Long medecinId = request.getMedecinId();
        if (medecinId == null) {
            throw new BadRequestException("L'identifiant du médecin est requis.");
        }

        Medecin medecin = medecinservices.findById(medecinId);

        Cabinet cabinet = cabinetrepo.findById(request.getCabinetId() != null ? request.getCabinetId() : medecin.getFirstCabinetId())
                .orElseThrow(() -> new Exception("Aucun cabinet correspondant trouvé."));

        List<MedecinSchedule> createdSchedules = new ArrayList<>();

        for (DayOfWeek day : request.getAvailableDays()) {
            for (MedecinMultiScheduleRequest.TimeSlot slot : request.getTimeSlots()) {
                if (slot.getEndTime().isBefore(slot.getStartTime())) {
                    throw new BadRequestException("L'heure de fin doit être après l'heure de début pour le créneau: "
                            + slot.getStartTime() + " - " + slot.getEndTime());
                }

                List<MedecinSchedule> existingSchedules = medecinScheduleRepository.findByMedIdAndDay(medecin.getId(), day.getValue() - 1);
                if (existingSchedules != null) {
                    for (MedecinSchedule existingSchedule : existingSchedules) {
                        MedecinScheduleRequest tempRequest = new MedecinScheduleRequest();
                        tempRequest.setAvailabilityStart(slot.getStartTime());
                        tempRequest.setAvailabilityEnd(slot.getEndTime());
                        tempRequest.setDay(day.toString());

                        if (isConflict(existingSchedule, tempRequest)) {
                            throw new ConflictException("Ce créneau entre en conflit avec un créneau existant pour le jour: " + day);
                        }
                    }
                }

                MedecinSchedule schedule = new MedecinSchedule();
                schedule.setDay(day);
                schedule.setAvailabilityStart(slot.getStartTime());
                schedule.setAvailabilityEnd(slot.getEndTime());
                schedule.setPeriod(slot.getPeriod());
                schedule.setModeconsultation(request.getModesConsultation());
                schedule.setMotifConsultation(request.getMotifsConsultation());
                schedule.setIsnewpatient(request.getAllowNewPatients());
                schedule.setIsFollowUpPatients(request.getAllowFollowUpPatients());
                schedule.setMedecin(medecin);
                schedule.setCabinet(cabinet);

                medecinScheduleRepository.save(schedule);
                createdSchedules.add(schedule);

                activityServices.createActivity(
                        new Date(),
                        "Add",
                        "Create New Schedule ID :" + schedule.getId(),
                        globalVariables.getConnectedUser()
                );
                LOGGER.info("Create New Schedule ID:" + schedule.getId() +
                        ", Par l'utilisateur ID:" +
                        (globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId() : ""));
            }
        }

        return createdSchedules;
    }

    public List<MedecinSchedule> updateOrDuplicateSchedule(MedecinMultiScheduleRequest request) throws Exception {
        if (request.getId() == null) {
            throw new BadRequestException("L'ID du planning à modifier est requis.");
        }

        LOGGER.info("Updating schedule ID: {}", request.getId());
        MedecinSchedule originalSchedule = medecinScheduleRepository.getById(request.getId());

        LOGGER.info("Original schedule: {}", originalSchedule.getId());

        Medecin medecin = medecinservices.findById(request.getMedecinId());
        Cabinet cabinet = cabinetrepo.findById(medecin.getFirstCabinetId())
                .orElseThrow(() -> new NotFoundException("Cabinet introuvable."));

        // Étape 1 : supprimer uniquement le schedule sélectionné
        LOGGER.info("Deleting schedule ID: {}", originalSchedule.getId());
        medecinScheduleRepository.deleteById(originalSchedule.getId());

        LOGGER.info("Deleted schedule ID success : {}", originalSchedule.getId());

        List<MedecinSchedule> resultSchedules = new ArrayList<>();

        for (DayOfWeek day : request.getAvailableDays()) {
            List<MedecinSchedule> existingSchedules = medecinScheduleRepository.findByMedIdAndDay(medecin.getId(), day.getValue() - 1);

            for (MedecinMultiScheduleRequest.TimeSlot slot : request.getTimeSlots()) {

                if (slot.getEndTime().isBefore(slot.getStartTime())) {
                    throw new BadRequestException("Heure de fin avant début : " + slot.getStartTime() + " → " + slot.getEndTime());
                }

                for (MedecinSchedule existing : existingSchedules) {
                    if (overlaps(slot.getStartTime(), slot.getEndTime(), existing.getAvailabilityStart(), existing.getAvailabilityEnd())) {
                        throw new ConflictException("Conflit avec un autre créneau existant pour le jour : " + day);
                    }
                }

                // Création du nouveau planning
                MedecinSchedule newSchedule = new MedecinSchedule();
                newSchedule.setDay(day);
                newSchedule.setAvailabilityStart(slot.getStartTime());
                newSchedule.setAvailabilityEnd(slot.getEndTime());
                newSchedule.setPeriod(slot.getPeriod());
                newSchedule.setModeconsultation(request.getModesConsultation());
                newSchedule.setMotifConsultation(request.getMotifsConsultation());
                newSchedule.setIsnewpatient(request.getAllowNewPatients());
                newSchedule.setIsFollowUpPatients(request.getAllowFollowUpPatients());
                newSchedule.setMedecin(medecin);
                newSchedule.setCabinet(cabinet);

                medecinScheduleRepository.save(newSchedule);
                resultSchedules.add(newSchedule);

                activityServices.createActivity(
                        new Date(),
                        "Add",
                        "Nouveau créneau recréé pour le jour : " + day + " ID: " + newSchedule.getId(),
                        globalVariables.getConnectedUser()
                );

                LOGGER.info("Recréé schedule jour: {}, début: {}, fin: {}, ID: {}",
                        newSchedule.getDay(),
                        newSchedule.getAvailabilityStart(),
                        newSchedule.getAvailabilityEnd(),
                        newSchedule.getId());
            }
        }

        return resultSchedules;
    }

    private boolean overlaps(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
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

   /* public List<Medecin> filterMedecinsByAvailability(List<Long> medecinIds, String filter) {
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
    }*/

    public List<Medecin> filterMedecinsByAvailability(List<Long> medecinIds, List<String> filters) {
        // Récupérez les plannings filtrés pour chaque filtre
        Map<Long, List<MedecinSchedule>> filteredSchedules = new HashMap<>();

        for (String filter : filters) {
            Map<Long, List<MedecinSchedule>> schedulesForFilter = filterSchedulesByAvailability(medecinIds, filter);
            schedulesForFilter.forEach((key, value) ->
                    filteredSchedules.merge(key, value, (existing, newSchedules) -> {
                        existing.addAll(newSchedules);
                        return existing;
                    })
            );
        }

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





}

