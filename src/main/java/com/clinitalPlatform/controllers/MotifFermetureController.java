package com.clinitalPlatform.controllers;

import com.clinitalPlatform.enums.MotifFermetureEnum;
import com.clinitalPlatform.models.MotifFermeture;
import com.clinitalPlatform.services.MotifFermetureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/motifs-fermeture")
public class MotifFermetureController {

    @Autowired
    private MotifFermetureService motifService;

    @GetMapping
    public List<MotifFermeture> getAllMotifs() {
        return motifService.findAll();
    }

    @PostMapping("/create")
    public MotifFermeture createMotif(@RequestBody MotifFermeture motif) {
        return motifService.save(motif);
    }

    @GetMapping("/{id}")
    public MotifFermeture getMotifById(@PathVariable Long id) {
        return motifService.findById(id);
    }

    @GetMapping("/by-motif/{motif}")
    public MotifFermeture getMotifByMotif(@PathVariable MotifFermetureEnum motif) {
        try {
            return motifService.findByMotif(motif);
        } catch (IllegalArgumentException e) {
            return null; // or handle the error as needed
        }
    }

    @PutMapping("/update/{id}")
    public MotifFermeture updateMotif(@PathVariable Long id, @RequestBody MotifFermeture motif) {
        return motifService.update(id, motif);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteMotif(@PathVariable Long id) {
        MotifFermeture motif = motifService.findById(id);
        if (motif != null) {
            motifService.delete(motif);
        }
    }
}

