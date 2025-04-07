package com.clinitalPlatform.controllers;

import com.clinitalPlatform.models.ModeConsultation;
import com.clinitalPlatform.enums.ModeConsultationEnum;
import com.clinitalPlatform.services.ModeConsultationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/mode-consultations")
public class ModeConsultationController {

    @Autowired
    private ModeConsultationService modeConsultationService;

    @GetMapping
    public ResponseEntity<List<ModeConsultation>> getAllModeConsultations() {
        List<ModeConsultation> modeConsultations = modeConsultationService.getAllModeConsultations();
        return new ResponseEntity<>(modeConsultations, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModeConsultation> getModeConsultationById(@PathVariable Long id) {
        Optional<ModeConsultation> modeConsultation = modeConsultationService.getModeConsultationById(id);
        return modeConsultation
                .map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/mode/{mode}")
    public ResponseEntity<ModeConsultation> getModeConsultationByMode(@PathVariable ModeConsultationEnum mode) {
        ModeConsultation modeConsultation = modeConsultationService.getModeConsultationByMode(mode);
        return modeConsultation != null
                ? new ResponseEntity<>(modeConsultation, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<ModeConsultation> createModeConsultation(@RequestBody ModeConsultation modeConsultation) {
        ModeConsultation newModeConsultation = modeConsultationService.saveModeConsultation(modeConsultation);
        return new ResponseEntity<>(newModeConsultation, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModeConsultation> updateModeConsultation(@PathVariable Long id, @RequestBody ModeConsultation modeConsultation) {
        if (!modeConsultationService.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        modeConsultation.setId_mode(id);
        ModeConsultation updatedModeConsultation = modeConsultationService.saveModeConsultation(modeConsultation);
        return new ResponseEntity<>(updatedModeConsultation, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModeConsultation(@PathVariable Long id) {
        if (!modeConsultationService.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        modeConsultationService.deleteModeConsultation(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}