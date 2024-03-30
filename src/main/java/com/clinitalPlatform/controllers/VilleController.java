package com.clinitalPlatform.controllers;

import com.clinitalPlatform.dto.VilleDTO;
import com.clinitalPlatform.repository.VilleRepository;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ville")
public class VilleController {

    @Autowired
    VilleRepository villerepo;
    @Autowired
    ClinitalModelMapper mapper;
    @Autowired
    ActivityController activityController;
    @Autowired
    GlobalVariables globalVariables;


    @GetMapping("/allvilles")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    Iterable<VilleDTO> villes() throws Exception {

        return villerepo.findAll().stream().map(ville ->mapper.map(ville, VilleDTO.class))
                .collect(Collectors.toList());
    }
}
