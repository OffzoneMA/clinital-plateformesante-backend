package com.clinitalPlatform.controllers;

import com.clinitalPlatform.exception.BadRequestException;
import com.clinitalPlatform.exception.ConflictException;
import com.clinitalPlatform.models.Demande;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.MedecinSchedule;
import com.clinitalPlatform.payload.request.FilterRequest;
import com.clinitalPlatform.payload.request.MedecinMultiScheduleRequest;
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
import jakarta.validation.Valid;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
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
    public ResponseEntity<?> create(@Validated @RequestBody MedecinScheduleRequest medecinScheduleRequest) {
        try {
            MedecinSchedule medecinSchedule = medecinScheduleService.create(medecinScheduleRequest, globalVariables.getConnectedUser().getId());
            return ResponseEntity.ok(modelMapper.map(medecinSchedule, MedecinSchedule.class));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Ce créneau entre en conflit avec un créneau existant.");
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur interne est survenue.");
        }
    }

    @PostMapping("multi")
    public ResponseEntity<?> createMultiSchedule(@RequestBody MedecinMultiScheduleRequest request) {
        try {
            Long userId = globalVariables.getConnectedUser().getId(); // Or however you get the current user ID
            List<MedecinSchedule> createdSchedules = medecinScheduleService.createMultiSchedule(request, userId);
            return ResponseEntity.ok(new ApiResponse(true, "Multiple schedules created successfully", createdSchedules));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PostMapping("multi-for-medecin")
    public ResponseEntity<?> createMultiForMedecin(@Valid @RequestBody MedecinMultiScheduleRequest request) {
        try {
            List<MedecinSchedule> created = medecinScheduleService.createMultiScheduleForMedecin(request);
            return ResponseEntity.ok(created);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Erreur lors de la création des créneaux : " + ex.getMessage());
        }
    }

    @PutMapping("/multi-update")
    public ResponseEntity<?> updateMultiSchedule(@Valid @RequestBody MedecinMultiScheduleRequest request) {
        try {
            List<MedecinSchedule> updated = medecinScheduleService.updateOrDuplicateSchedule(request);
            return ResponseEntity.ok(Map.of(
                    "message", "Mise à jour effectuée avec succès.",
                    "updatedSchedules", updated
            ));
        } catch (BadRequestException | NotFoundException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Erreur serveur interne."));
        }
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
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        try {
            // Vérification de l'ID
            if (id == null || id <= 0) {
                throw new BadRequestException("L'ID fourni est invalide.");
            }

            // Appel du service pour supprimer le planning
            medecinScheduleService.deleteById(id);

            // Retourner une réponse de succès
            return ResponseEntity.ok(new ApiResponse(true, "Schedule has been deleted successfully"));
        } catch (NotFoundException e) {
            // Gestion de l'exception si l'élément n'est pas trouvé
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, e.getMessage()));
        } catch (BadRequestException e) {
            // Gestion de l'exception pour une requête invalide
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            // Gestion des erreurs internes
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Une erreur interne est survenue."));
        }
    }

    @GetMapping("shedulebyMed")
    @ResponseBody
    public ResponseEntity<?> getAllSchedulesByMedId() throws Exception{
        System.out.println("med");
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
    @GetMapping("shedulesByIdMedDay/{id}")
    @ResponseBody
    public ResponseEntity<?> GetSchedulesByDay(@PathVariable long id, @RequestBody MedecinScheduleRequest medecinScheduleRequest) throws Exception{
        // UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        //return ResponseEntity.ok(DayOfWeek.valueOf(medecinScheduleRequest.getDay()).getValue());
//        return ResponseEntity.ok(medshrep.findByMedIdAndDay(id , DayOfWeek.valueOf(medecinScheduleRequest.getDay()).getValue())
//                .stream().map(sched -> modelMapper.map(sched, MedecinSchedule.class))
//                .collect(Collectors.toList()));
        return ResponseEntity.ok(medshrep.findByMedIdAndDay(id, DayOfWeek.valueOf(medecinScheduleRequest.getDay()).getValue()-1)
                .stream().map(sched -> modelMapper.map(sched, MedecinSchedule.class))
                .collect(Collectors.toList()));


    }
    @GetMapping("fromCreno")
    public ResponseEntity<?> getScheduleFromCreno(@RequestParam("creno") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime creno,
                                                  @RequestParam("day") DayOfWeek day,
                                                  @RequestParam("idmed") long idmed) {
        try {
            // Appeler la fonction pour récupérer le planning
            MedecinSchedule schedule = medecinScheduleService.getScheduleFromCreno(creno.toLocalTime(), day, idmed);
            if (schedule != null) {
                // Planning trouvé, retourner le résultat
                return ResponseEntity.ok(schedule);
            } else {
                // Planning non trouvé pour ce créneau
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            // Gérer les erreurs
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la récupération du planning.");
        }
    }

    //FILTRE DISPONIBILITÉ--------------------------------------------

   /* @GetMapping("/medecins/schedules/filter")
    public ResponseEntity<Map<Long, List<MedecinSchedule>>> filterMedecinSchedulesByAvailability(
            @RequestParam List<Long> medecinIds,
            @RequestParam String filter
    ) {
        Map<Long, List<MedecinSchedule>> filteredSchedules = medecinScheduleService.filterSchedulesByAvailability(medecinIds, filter);
        return new ResponseEntity<>(filteredSchedules, HttpStatus.OK);
    }*/

   /* @PostMapping("/medecins/schedules/filter")
    public ResponseEntity<Map<Long, List<MedecinSchedule>>> filterMedecinSchedulesByAvailability(
            @RequestBody FilterRequest filterRequest
    ) {
        List<Long> medecinIds = filterRequest.getMedecinIds();
        String filter = filterRequest.getFilter();
        System.out.println("ici:"+filter);
        System.out.println("d"+medecinIds);

        // Utilisez medecinIds et filter pour filtrer les plannings des médecins
        Map<Long, List<MedecinSchedule>> filteredSchedules = medecinScheduleService.filterSchedulesByAvailability(medecinIds, filter);

        //return ResponseEntity.ok(new ApiResponse(false, "No matching Found"));
        System.out.println("OKOKOKOKOK");

        return new ResponseEntity<>(filteredSchedules, HttpStatus.OK);
    }*/

    @PostMapping("/medecins/schedules/filter")
    public ResponseEntity<List<Medecin>> filterMedecinSchedulesByAvailability(
            @RequestBody FilterRequest filterRequest
    ) {
        List<Long> medecinIds = filterRequest.getMedecinIds();
        List<String> filters = filterRequest.getFilters(); // Liste de filtres

        System.out.println("Liste des filtres : " + filters);
        System.out.println("IDs des médecins : " + medecinIds);

        // Utilisez medecinIds et filters pour filtrer les plannings des médecins
        List<Medecin> filteredMedecins = medecinScheduleService.filterMedecinsByAvailability(medecinIds, filters);

        // Retournez la réponse avec les médecins filtrés
        return new ResponseEntity<>(filteredMedecins, HttpStatus.OK);
    }







}
