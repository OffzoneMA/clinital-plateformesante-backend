package com.clinitalPlatform.controllers;

import com.clinitalPlatform.dto.MedecinNetworkDTO;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.MedecinNetwork;
import com.clinitalPlatform.payload.request.networkRequest;
import com.clinitalPlatform.services.MedecinNetworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/MedNetwork")
public class MedecinNetworkController {

    @Autowired
    private MedecinNetworkService medecinNetworkService;

    @PostMapping("/addNewNetwork")
    public ResponseEntity<MedecinNetwork> addMedecinNetwork(@RequestBody networkRequest request, @RequestParam long userId) {
        try {
            MedecinNetwork medecinNetwork = medecinNetworkService.addMedecinNetwork(request, userId);
            return ResponseEntity.ok(medecinNetwork);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<MedecinNetworkDTO> updateMedecinNetwork(@RequestBody MedecinNetworkDTO medecinNetworkDTO) {
        try {
            MedecinNetworkDTO updatedNetwork = medecinNetworkService.updateMedecinNetwork(medecinNetworkDTO);
            return ResponseEntity.ok(updatedNetwork);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteMedecinNetwork(@RequestParam Long idMedecin, @RequestParam Long idFollower) {
        try {
            medecinNetworkService.deleteMedecinNetwork(idMedecin, idFollower);
            return ResponseEntity.ok("Network deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<?>> getAllMedecinNetwork(@RequestParam Long idMedecin) {
        try {
            List<?> medecinNetworks = medecinNetworkService.getAllMedecinNetwork(idMedecin);
            return ResponseEntity.ok(medecinNetworks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/follower")
    public ResponseEntity<Medecin> getMedecinFollowerById(@RequestParam Long idMedecin, @RequestParam Long idFollower) {
        try {
            Medecin follower = medecinNetworkService.getMedecinfollewerById(idMedecin, idFollower);
            return ResponseEntity.ok(follower);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/network")
    public ResponseEntity<MedecinNetwork> findMedecinsNetworkByID(@RequestParam Long idMedecin, @RequestParam Long idFollower) {
        try {
            MedecinNetwork medecinNetwork = medecinNetworkService.FindMedecinsNetworkByID(idMedecin, idFollower);
            return ResponseEntity.ok(medecinNetwork);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
