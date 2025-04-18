package com.clinitalPlatform.services.interfaces;

import com.clinitalPlatform.dto.MedecinNetworkDTO;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.MedecinNetwork;
import com.clinitalPlatform.payload.request.NetworkRequest;

import java.util.List;

public interface NetworkService {

    public MedecinNetwork addMedecinNetwork(NetworkRequest medecinNetwork, long id) throws Exception;

    public MedecinNetworkDTO updateMedecinNetwork(MedecinNetworkDTO medecinNetworkDTO) throws Exception;

    public void deleteMedecinNetwork(Long id_medecin, Long id_follower) throws Exception;

    public List<?> getAllMedecinNetwork(Long id_medecin) throws Exception;

    public Medecin getMedecinfollewerById(Long id_medecin, Long id_follower) throws Exception;

    public MedecinNetwork FindMedecinsNetworkByID(Long id_medecin, Long id_follower) throws Exception;

}
