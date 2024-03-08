package com.clinitalPlatform.services;

import com.clinitalPlatform.models.MedecinSchedule;
import com.clinitalPlatform.payload.request.MedecinScheduleRequest;
import com.clinitalPlatform.repository.MedecinScheduleRepository;
import com.clinitalPlatform.services.interfaces.MedecinScheduleService;
import com.clinitalPlatform.util.ClinitalModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Transactional
@Service
public class MedecinScheduleServiceImpl implements MedecinScheduleService {
    @Override
    public MedecinSchedule create(MedecinScheduleRequest medecinScheduledrequest, Long id) throws Exception {
        return null;
    }

    @Override
    public MedecinSchedule update(MedecinScheduleRequest medecinScheduledrequest, Long id) throws Exception {
        return null;
    }

    @Override
    public void deleteById(Long id) throws Exception {

    }

    //@Autowired
//    private MedecinScheduleRepository medecinScheduleRepository;
//
//    @Autowired
//    private ModeConsultRespository moderepo;
//
//    @Autowired
//    private ClinitalModelMapper modelMapper;
//    @Autowired
//    private TypeConsultationServicesImpli typeConsult;
//    @Autowired
//    private MedecinServiceImpl medecinservices;
//    @Autowired
//    private CabinetRepository cabinetrepo;
//
//    @PersistenceContext
//    private EntityManager entityManger;
//
//    @Autowired
//    private GlobalVariables globalVariables;
//
//    @Autowired
//    private ActivityServices activityServices;
//
//    private final Logger LOGGER= LoggerFactory.getLogger(getClass());
//
//    // Creating a new instance of MedecinSchedule.
//    @Override
//    public MedecinSchedule create(@Valid MedecinScheduleRequest medecinScheduleRequest, Long id) throws Exception {
//        // Create a new instance of MedecinSchedule
//        MedecinSchedule schedule = new MedecinSchedule();
//        Medecin Med = medecinservices.getMedecinByUserId(id);
//        Cabinet cabinet=cabinetrepo.findById(medecinScheduleRequest.getCabinet_id())
//                .orElseThrow(()->new BadRequestException("No matching found for this Cabinet"));
//
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
//        // medecinScheduleRepository.save(schedule);
//        // Save it
//        entityManger.persist(schedule);
//        // MedecinSchedule medecinSchedule1 = medecinScheduleRepository.save(schedule);
//        activityServices.createActivity(new Date(),"Add","Create New Schedule ID :"+schedule.getId(),globalVariables.getConnectedUser());
//        LOGGER.info("Create New Schedule ID :"+schedule.getId()+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
//        return modelMapper.map(schedule, MedecinSchedule.class);
//    }
//
//    // Updating the MedecinSchedule.
//    @Override
//    public MedecinSchedule update(@Valid MedecinScheduleRequest req, Long id) throws Exception {
//        Optional<MedecinSchedule> IsSchedule = medecinScheduleRepository.findById(id);
//        if (IsSchedule.isPresent()) {
//
//            Medecin Med = medecinservices.findById(req.getMedecin_id());
//            MedecinSchedule schedule = medecinScheduleRepository.getById(id);
//            Cabinet cabinet=cabinetrepo.findById(req.getCabinet_id())
//                    .orElseThrow(()->new BadRequestException("No matching found for this Cabinet"));
//            // Set attribut of MedecinSchedule
//            // Set attribut of MedecinSchedule
//            schedule.setDay(DayOfWeek.valueOf(req.getDay()));
//            schedule.setAvailabilityStart(req.getAvailabilityStart());
//            schedule.setAvailabilityEnd(req.getAvailabilityEnd());
//            schedule.setModeconsultation(req.getModeconsultation());
//            schedule.setPeriod(req.getPeriod());
//            schedule.setMedecin(Med);
//            schedule.setCabinet(cabinet);
//            schedule.setIsnewpatient(req.getIsnewpatient());
//            medecinScheduleRepository.save(schedule);
//
//            activityServices.createActivity(new Date(),"Update","Update New Schedule ID :"+schedule.getId(),globalVariables.getConnectedUser());
//            LOGGER.info("Update Schedule ID :"+schedule.getId()+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
//
//            return schedule;
//
//        } else
//            activityServices.createActivity(new Date(),"Error","Cant Update Schedule",globalVariables.getConnectedUser());
//        LOGGER.error("Error to Update Schedule , UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
//        throw new Exception("Failed to update");
//    }
//
//    @Override
//    public void deleteById(Long id) throws Exception {
//        Optional<MedecinSchedule> medecinSchedule = medecinScheduleRepository.findById(id);
//        if (medecinSchedule.isPresent()) {
//            medecinScheduleRepository.deleteById(id);
//            activityServices.createActivity(new Date(),"Delete","Delete Schedule ID :"+id,globalVariables.getConnectedUser());
//            LOGGER.info("Delete Schedule ID :"+id+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
//        } else
//            activityServices.createActivity(new Date(),"Warning","Cant Delete Schedule ID :"+id,globalVariables.getConnectedUser());
//        LOGGER.warn("Cant Delete Schedule ID :"+id+", UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
//        throw new Exception("Fail to delete");
//    }
//
//    // A method that returns a list of MedecinSchedule objects by id Medecin.
//    public List<MedecinSchedule> GetAllSchedulesByMedId(long idmed) throws Exception {
//        activityServices.createActivity(new Date(),"Read","Consulting All Schedules",globalVariables.getConnectedUser());
//        LOGGER.info("Consulting All Schedules , UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
//        return medecinScheduleRepository.GetAllSchedulesByMedId(idmed)
//                .stream()
//                .map(schedule -> modelMapper.map(schedule, MedecinSchedule.class))
//                .collect(Collectors.toList());
//
//    }
//
//    // Returning a single `MedecinSchedule` object.
//    public MedecinSchedule GetAllSchedulesByIdsched(long idsched) throws Exception {
//        MedecinSchedule medecinSchedule=medecinScheduleRepository.findById(idsched).orElseThrow(()->new Exception("No Matching Found"));
//        activityServices.createActivity(new Date(),"Read","Consulting Schedule by id :"+idsched,globalVariables.getConnectedUser());
//        LOGGER.info("Consulting Schedule by id :"+idsched+" , UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
//        return medecinSchedule;
//
//    }
//
//    // A method that returns a list of MedecinSchedule objects by id Medecin and id
//    // TypeConsultation.
//    public List<MedecinSchedule> GetAllSchedulesByMedIdandIdCOnsult(long idmed, long idconsult) throws Exception {
//
//        activityServices.createActivity(new Date(),"Read","Consulting Schedule by id Med :"+idmed+" and ID Consultation : "+idconsult,globalVariables.getConnectedUser());
//        LOGGER.info("Consulting Schedule by id Med :"+idmed+" and ID Consultation : "+idconsult+" , UserID : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
//        return medecinScheduleRepository.GetAllSchedulesByMedIdandIdCOnsult(idmed, idconsult)
//                .stream()
//                .map(schedule -> modelMapper.map(schedule, MedecinSchedule.class))
//                .collect(Collectors.toList());
//
//    }
}

