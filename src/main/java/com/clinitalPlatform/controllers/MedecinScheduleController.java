package com.clinitalPlatform.controllers;

import com.clinitalPlatform.models.Demande;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.MedecinSchedule;
import com.clinitalPlatform.payload.request.MedecinScheduleRequest;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.repository.MedecinScheduleRepository;
import com.clinitalPlatform.services.MedecinScheduleServiceImpl;
import com.clinitalPlatform.services.MedecinServiceImpl;
import com.clinitalPlatform.util.ApiError;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/medecinSchedule/")
public class MedecinScheduleController {

    @Autowired
    private MedecinScheduleServiceImpl medecinScheduleService;

    @Autowired
    private MedecinScheduleRepository medshrep;

    @Autowired
    private ClinitalModelMapper modelMapper;

    @Autowired
    private MedecinServiceImpl medservice;


    @Autowired
    GlobalVariables globalVariables;
    // Creat a new Schedule : %OK%
    @PostMapping("create")
    //@PreAuthorize("hasAuthority('ROLE_MEDECIN')")
    public ResponseEntity<?> create(@Validated @RequestBody MedecinScheduleRequest
                                            medecinScheduleRequest) throws Exception{

        MedecinSchedule medecinSchedule = medecinScheduleService.create(medecinScheduleRequest,globalVariables.getConnectedUser().getId());
        return ResponseEntity.ok(modelMapper.map(medecinSchedule,MedecinSchedule.class));
    }


    @PostMapping("update/{id}")
    @ResponseBody
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    public ResponseEntity<?> update(@Validated @RequestBody MedecinScheduleRequest medecinScheduleRequest, @PathVariable Long id) throws Exception {
        try {
            MedecinSchedule isSchedule=medshrep.getById(id);
            if(isSchedule!=null){
                return ResponseEntity.ok(medecinScheduleService.update(medecinScheduleRequest,id));
            }else{
                return ResponseEntity.accepted().body(new ApiError(HttpStatus.BAD_REQUEST, "update failed", null));
            }
        } catch (Exception e) {
            // TODO: handle exception
            throw new Exception(e);
        }



    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteById( @PathVariable Long id) throws Exception{

        medecinScheduleService.deleteById(id);

        return ResponseEntity.ok(new ApiResponse(true, "Schedule has been deleted successfully"));

    }

    @GetMapping("shedulebyMed")
    @ResponseBody
    public ResponseEntity<?> getAllSchedulesByMedId() throws Exception{
        Medecin med = medservice.getMedecinByUserId(globalVariables.getConnectedUser().getId());
        try {
            return ResponseEntity.ok(medecinScheduleService.getAllSchedulesByMedId(med.getId())
                    .stream().map(sched -> modelMapper.map(sched, MedecinSchedule.class))
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            throw new Exception(e);
        }
    }
    @GetMapping("shedulebyMedandIdconsult/{id}")
    @ResponseBody
    public ResponseEntity<?> getAllSchedulesByMedIdandIdConsult(@PathVariable long id) throws Exception{
        Medecin med = medservice.getMedecinByUserId(globalVariables.getConnectedUser().getId());
        try {
            return ResponseEntity.ok(medecinScheduleService.getAllSchedulesByMedIdandIdConsult(med.getId(),id)
                    .stream().map(sched -> modelMapper.map(sched, MedecinSchedule.class))
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @GetMapping("shedulebyId/{id}")
    @ResponseBody
    public ResponseEntity<?> GetAllSchedulesByid(@PathVariable long id) throws Exception{
        // UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Medecin med = medservice.getMedecinByUserId(globalVariables.getConnectedUser().getId());
        try {
            Optional<MedecinSchedule> Sched = medshrep.findById(id);
            if(Sched.isPresent()){
                return ResponseEntity.ok(modelMapper.map(medecinScheduleService.GetAllSchedulesByIdsched(id), MedecinSchedule.class));
            }
            else{
                return ResponseEntity.ok(new ApiResponse(false, "No matching Found"));
            }
        } catch (Exception e) {
            throw new Exception(e);
        }

    }
}
