package com.clinitalPlatform.services.interfaces;

import com.clinitalPlatform.models.Langue;
import com.clinitalPlatform.models.Medecin;

import java.util.List;

public interface LangueService {

    public Langue findById(Long id) throws Exception;
    List<Medecin> findMedecinsByLangueId(Long langueId)throws Exception;



}
