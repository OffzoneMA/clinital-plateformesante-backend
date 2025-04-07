package com.clinitalPlatform.controllers;

import com.clinitalPlatform.models.MotifConsultation;
import com.clinitalPlatform.enums.MotifConsultationEnum;
import com.clinitalPlatform.services.MotifConsultationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/motif-consultations")
public class MotifConsultationController {

    @Autowired
    private MotifConsultationService motifConsultationService;

    @GetMapping
    public ResponseEntity<List<MotifConsultation>> getAllMotifConsultations() {
        List<MotifConsultation> motifConsultations = motifConsultationService.getAllMotifConsultations();
        return new ResponseEntity<>(motifConsultations, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MotifConsultation> getMotifConsultationById(@PathVariable Long id) {
        Optional<MotifConsultation> motifConsultation = motifConsultationService.getMotifConsultationById(id);
        return motifConsultation
                .map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/motif/{motif}")
    public ResponseEntity<MotifConsultation> getMotifConsultationByMotif(@PathVariable MotifConsultationEnum motif) {
        MotifConsultation motifConsultation = motifConsultationService.getMotifConsultationByMotif(motif);
        return motifConsultation != null
                ? new ResponseEntity<>(motifConsultation, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<MotifConsultation> createMotifConsultation(@RequestBody MotifConsultation motifConsultation) {
        MotifConsultation newMotifConsultation = motifConsultationService.saveMotifConsultation(motifConsultation);
        return new ResponseEntity<>(newMotifConsultation, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MotifConsultation> updateMotifConsultation(@PathVariable Long id, @RequestBody MotifConsultation motifConsultation) {
        if (!motifConsultationService.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        motifConsultation.setId_motif(id);
        MotifConsultation updatedMotifConsultation = motifConsultationService.saveMotifConsultation(motifConsultation);
        return new ResponseEntity<>(updatedMotifConsultation, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMotifConsultation(@PathVariable Long id) {
        if (!motifConsultationService.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        motifConsultationService.deleteMotifConsultation(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}