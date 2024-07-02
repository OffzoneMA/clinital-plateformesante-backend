package com.clinitalPlatform.controllers;

import com.clinitalPlatform.models.Tarif;
import com.clinitalPlatform.payload.request.TarifRequest;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.services.TarifServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
@RequestMapping("/api/tarifmed")
public class TarifController {
    @Autowired
    private TarifServiceImpl tarifService;

    @PostMapping("/add")
    public ResponseEntity<Tarif> addTarif(@RequestBody TarifRequest tarifRequest) {
        try {
            Tarif newTarif = tarifService.save(tarifRequest);
            return ResponseEntity.ok(newTarif);
        } catch (Exception e) {
            log.error("Error while adding tarif", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update/{tarifId}")
    public ResponseEntity<Tarif> updateTarif(@PathVariable Long tarifId, @RequestBody TarifRequest updatedTarifRequest) {
        try {
            Tarif updated = tarifService.updateTarif(tarifId, updatedTarifRequest);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error while updating tarif with ID: {}", tarifId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/{tarifId}")
    public ResponseEntity<?> getTarifById(@PathVariable Long tarifId) {
        Tarif tarif = tarifService.findById(tarifId);
        if (tarif != null) {
            return ResponseEntity.ok(tarif);
        } else {
            log.error("No tarif found with ID: {}", tarifId);
            return ResponseEntity.ok(new ApiResponse(false, "Aucun tarif trouvé"));
        }
    }



    @DeleteMapping("/delete/{tarifId}")
    public ResponseEntity<?> deleteTarif(@PathVariable Long tarifId) {
        try {
            tarifService.deleteTarif(tarifId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error while deleting tarif with ID: {}", tarifId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}
