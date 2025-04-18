package com.clinitalPlatform.services;

import com.clinitalPlatform.dto.MedecinNetworkDTO;
import com.clinitalPlatform.exception.BadRequestException;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.MedecinNetwork;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.payload.request.NetworkRequest;
import com.clinitalPlatform.repository.MedecinNetworkRepository;
import com.clinitalPlatform.repository.MedecinRepository;
import com.clinitalPlatform.services.interfaces.NetworkService;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Transactional
@Service
public class MedecinNetworkService implements NetworkService {

    @Autowired
    private MedecinNetworkRepository medecinNetworkRepository;

    @Autowired
    private MedecinRepository medecinRepository;

    @Autowired
    private ClinitalModelMapper clinitalModelMapper;

    @Autowired
    private ActivityServices activityServices;

    @Autowired
    private GlobalVariables globalVariables;
    private final Logger LOGGER=LoggerFactory.getLogger(getClass());

    public MedecinNetworkService() {
        super();
    }

    @Override
    public MedecinNetwork addMedecinNetwork(NetworkRequest medecinNetwork, long user_id) throws Exception {
        Medecin med = medecinRepository.getMedecinByUserId(user_id);
        Medecin follower = medecinRepository.getMedecinById(medecinNetwork.getFollower_id());

        LOGGER.info("Add Medecin follower to Network by id " + medecinNetwork.getFollower_id() + " Comment : " + medecinNetwork.getComment() + " for Medecin Connected, User ID  : " + (globalVariables.getConnectedUser() != null ? globalVariables.getConnectedUser().getId() : ""));

        if (!Objects.equals(med.getId(), follower.getId())) {
            MedecinNetwork medecinNetworkEntity = new MedecinNetwork(med, follower, medecinNetwork.getComment());
            return medecinNetworkRepository.save(medecinNetworkEntity);
        } else {
            throw new BadRequestException("Not allowed");
        }
    }

    @Override
    public MedecinNetworkDTO updateMedecinNetwork(MedecinNetworkDTO medecinNetworkDTO) throws Exception {
        MedecinNetwork existingNetwork = medecinNetworkRepository.FindMedecinsNetworkByID(
                medecinNetworkDTO.getId().getId_medecin(), medecinNetworkDTO.getId().getId_follower());

        if (existingNetwork != null) {
            existingNetwork.setComment(medecinNetworkDTO.getComment());
            MedecinNetwork updatedNetwork = medecinNetworkRepository.save(existingNetwork);
            return clinitalModelMapper.map(updatedNetwork, MedecinNetworkDTO.class);
        } else {
            throw new BadRequestException("Network not found");
        }
    }

    public void deleteMedecinNetwork(Long id_medecin, Long id_follower) throws Exception {
        MedecinNetwork follower = medecinNetworkRepository.FindMedecinsNetworkByID(id_medecin, id_follower);

        if (follower != null) {
            activityServices.createActivity(new Date(), "Delete", "Delete Medecin from Network By ID : " + id_follower + "  for Connected Medecin Network", globalVariables.getConnectedUser());
            LOGGER.info("Delete Medecin follower from Network by id " + id_follower + " for Medecin Connected, User ID  : " + (globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId() : ""));
            medecinNetworkRepository.deleteNetworkById(follower.getMedecin().getId(), follower.getFollower().getId());
        } else {
            activityServices.createActivity(new Date(), "error", "Failed to Delete Medecin from Network By ID : " + id_follower + "  for Connected Medecin Network", globalVariables.getConnectedUser());
            LOGGER.error("Failed Delete Medecin follower from Network by id " + id_follower + " for Medecin Connected, User ID  : " + (globalVariables.getConnectedUser() instanceof User ? globalVariables.getConnectedUser().getId() : ""));
            throw new Exception("Fail to delete");
        }
    }


    @Override
    public List<?> getAllMedecinNetwork(Long id_medecin) throws Exception {
        return medecinRepository.getAllMedecinNetwork(id_medecin);
    }

    @Override
    public Medecin getMedecinfollewerById(Long id_medecin, Long id_follower) throws Exception {
        return medecinRepository.getMedecinsFollowerByID(id_medecin, id_follower);

    }

    @Override
    public MedecinNetwork FindMedecinsNetworkByID(Long id_medecin, Long id_follower) throws Exception {
        return  medecinNetworkRepository.FindMedecinsNetworkByID(id_medecin, id_follower);
    }
}
