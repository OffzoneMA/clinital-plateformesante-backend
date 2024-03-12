package com.clinitalPlatform.controllers;

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
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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
    private MedecinServiceImpl medserice;

    @Autowired
    private MedecinScheduleRepository medsrepo;

    @Autowired
    GlobalVariables globalVariables;
    // Creat a new Schedule : %OK%
    @PostMapping("create")
    @PostAuthorize("hasAuthority('ROLE_MEDECIN')")
    public MedecinSchedule create(@RequestBody MedecinScheduleRequest
                                            medecinScheduleRequest) throws Exception{

        MedecinSchedule medecinSchedule = medecinScheduleService.create(medecinScheduleRequest,globalVariables.getConnectedUser().getId());
        return medecinSchedule;
    }


    @PostMapping("update/{id}")
    @ResponseBody
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    //@PostAuthorize("hasAuthority('MEDECIN')")
    public ResponseEntity<?> update(@RequestBody MedecinScheduleRequest medecinScheduleRequest, @PathVariable Long id) throws Exception {

        try {
            Optional<MedecinSchedule> isSchedule=medsrepo.findById(id);
            if(isSchedule.isPresent()){
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
    public ResponseEntity<?> deleteById(@PathVariable Long id) throws Exception{

        medecinScheduleService.deleteById(id);

        return ResponseEntity.ok(new ApiResponse(true, "Schedule has been deleted successfully"));

    }

    @GetMapping("shedulebyMed")
    @ResponseBody
    public ResponseEntity<?> GetAllSchedulesByMedId() throws Exception{
        // UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Medecin med = medserice.getMedecinByUserId(globalVariables.getConnectedUser().getId());
        try {

            return ResponseEntity.ok(modelMapper.map(medecinScheduleService.GetAllSchedulesByMedId(med.getId()),MedecinSchedule.class));
        } catch (Exception e) {
            throw new Exception(e);
        }



    }
    @GetMapping("shedulebyMedandIdconsult/{id}")
    @ResponseBody
    public ResponseEntity<?> GetAllSchedulesByMedIdandIdConsult(@PathVariable long id) throws Exception{
        // UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Medecin med = medserice.getMedecinByUserId(globalVariables.getConnectedUser().getId());
        try {

            return ResponseEntity.ok(modelMapper.map(medecinScheduleService.GetAllSchedulesByMedIdandIdCOnsult(med.getId(),id), MedecinSchedule.class));


        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @GetMapping("shedulebyId/{id}")
    @ResponseBody
    public ResponseEntity<?> GetAllSchedulesByid(@PathVariable long id) throws Exception{
        // UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Medecin med = medserice.getMedecinByUserId(globalVariables.getConnectedUser().getId());
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
