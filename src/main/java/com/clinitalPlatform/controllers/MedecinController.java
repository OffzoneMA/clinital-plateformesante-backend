package com.clinitalPlatform.controllers;

import com.clinitalPlatform.dto.RendezvousDTO;
import com.clinitalPlatform.exception.BadRequestException;
import com.clinitalPlatform.models.MedecinSchedule;
import com.clinitalPlatform.models.Rendezvous;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.payload.response.AgendaResponse;
import com.clinitalPlatform.payload.response.GeneralResponse;
import com.clinitalPlatform.payload.response.HorairesResponse;
import com.clinitalPlatform.repository.MedecinRepository;
import com.clinitalPlatform.repository.MedecinScheduleRepository;
import com.clinitalPlatform.services.ActivityServices;
import com.clinitalPlatform.services.MedecinServiceImpl;
import com.clinitalPlatform.services.RendezvousService;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/med/")
@Slf4j
public class MedecinController {

	@Autowired
	MedecinRepository medrepository;

	@Autowired
	ClinitalModelMapper mapper;

	@Autowired
	RendezvousService rendezvousService;

	@Autowired
	MedecinScheduleRepository medScheduleRepo;

	@Autowired
	private MedecinServiceImpl medecinService;


	public static boolean checkday = false;

	@Autowired
	GlobalVariables globalVariables;

	@Autowired 
	private ActivityServices activityServices;

	private final Logger LOGGER=LoggerFactory.getLogger(getClass());
	// Get all medecins ... : %OK%




	// Finding all the schedules bY med Id from a given date.%OK%
	@GetMapping("/schedulesofMed/{idmed}")
	@JsonSerialize(using = LocalDateSerializer.class)  //@ApiParam(value = "startDate", example = "yyyy-MM-dd")
	public List<MedecinSchedule> findallSchudelesfromDate(@PathVariable Long idmed,
														  @PathVariable(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate startDate) throws Exception {
				if(globalVariables.getConnectedUser()!=null){
					activityServices.createActivity(new Date(),"Read","Consult Schedules of Medecin by is ID: "+idmed,globalVariables.getConnectedUser());
				LOGGER.info("Consult schedules of Medecin By his ID : "+idmed+" name by User : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
				}
		return medScheduleRepo
				.findByMedId(idmed)
				.stream()
				.map(item -> mapper.map(item, MedecinSchedule.class))
				.collect(Collectors.toList());

	}

	// get agenda bY med Id from a given date.%OK%
	@GetMapping("/agenda/{idmed}/{weeks}/{startDate}")
	@JsonSerialize(using = LocalDateSerializer.class)
	public List<AgendaResponse> GetCreno(@Validated @PathVariable long idmed, @PathVariable long weeks,

										 @PathVariable(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate startDate)
			throws Exception {

		try {

			List<AgendaResponse> agendaResponseList = new ArrayList<AgendaResponse>();
			List<MedecinSchedule> schedules = medScheduleRepo
					.findByMedId(idmed)
					.stream()
					.map(item -> mapper.map(item, MedecinSchedule.class))
					.collect(Collectors.toList());

					

			int days = medecinService.getDaysInMonth(startDate.atStartOfDay());

			for (int j = 1; j <= weeks; j++) {

				for (int i = 1; i <= 7; i++) {
					checkday = false;
					if (!schedules.isEmpty()) {
						for (MedecinSchedule medsch : schedules) {

							if (medsch.getDay().getValue() == startDate.getDayOfWeek().getValue()) {
								checkday = true;
								AgendaResponse agenda = new AgendaResponse();
								for (AgendaResponse ag : agendaResponseList) {
									if (ag.getDay().getValue() == medsch.getDay().getValue() && ag.getWeek() == j) {
										int index = agendaResponseList.indexOf(ag);
										agenda = agendaResponseList.get(index);
										agenda = medecinService.CreateCreno(medsch, agenda, idmed, j,
												startDate.atStartOfDay());
										agendaResponseList.set(index, agenda);

									}
								}
								agenda = medecinService.CreateCreno(medsch, agenda, idmed, j, startDate.atStartOfDay());
								// diffrance hourse :
								long Hours = ChronoUnit.HOURS.between(medsch.getAvailabilityStart(),
										medsch.getAvailabilityEnd());
								agenda.getMedecinTimeTable().add(new GeneralResponse("startTime",
										medsch.getAvailabilityStart()));
								agenda.getMedecinTimeTable().add(new GeneralResponse("endTime",
										medsch.getAvailabilityStart().plusHours(Hours)));
								String startTime = medsch.getAvailabilityStart().getHour() + ":"
										+ medsch.getAvailabilityStart().getMinute();

								String endTime = medsch.getAvailabilityEnd().getHour() + ":"
										+ medsch.getAvailabilityEnd().getMinute();

								agenda.getWorkingHours().add(new HorairesResponse(startTime,
										endTime));

								agendaResponseList.add(agenda);

								continue;

							}

						}
					}
					if (!checkday) {

						AgendaResponse agenda = new AgendaResponse();
						agenda.setDay(startDate.getDayOfWeek());
						agenda.setWorkingDate(startDate.atStartOfDay());
						agendaResponseList.add(agenda);
					}
					startDate = startDate.plusDays(1);//

				}

			}
			// Create a new LinkedHashSet
			Set<AgendaResponse> set = new LinkedHashSet<>();

			// Add the elements to set
			set.addAll(agendaResponseList);

			// Clear the list
			agendaResponseList.clear();

			// add the elements of set
			// with no duplicates to the list
			agendaResponseList.addAll(set);

			if(globalVariables.getConnectedUser()!=null){
				activityServices.createActivity(new Date(),"Read","Consult Medecin Agenda by his ID : "+idmed,globalVariables.getConnectedUser());
				LOGGER.info("Consult Medecin Agenda By his ID : "+idmed+" by User : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
			}

			return agendaResponseList;

		} catch (Exception e) {
			throw new BadRequestException("error :" + e);
		}

	}

	// Finding all the RDV bY med Id from a given date.%OK%
	@GetMapping("/rdvofMed/{idmed}/{startDate}")
	@JsonSerialize(using = LocalDateSerializer.class)
	public ResponseEntity<?> findallRDVforMedBystartDate(@PathVariable Long idmed,
																	 @PathVariable(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate startDate) throws Exception {

		LocalDateTime startDateTime = startDate.atStartOfDay();
		if(globalVariables.getConnectedUser()!=null){
			activityServices.createActivity(new Date(),"Read","Consult Medecin RDV By his ID : "+idmed,globalVariables.getConnectedUser());
			LOGGER.info("Consult Medecin RDV By his ID : "+idmed+" by User : "+(globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId():""));
		}

		return ResponseEntity.ok(rendezvousService
				.findRendezvousByMedAndDate(idmed, startDateTime)
				.stream().map(rdv -> mapper.map(rdv, RendezvousDTO.class))
				.collect(Collectors.toList()));

	}




}
