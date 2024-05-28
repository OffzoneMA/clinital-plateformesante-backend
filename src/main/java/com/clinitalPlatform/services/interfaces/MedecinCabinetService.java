package com.clinitalPlatform.services.interfaces;

import com.clinitalPlatform.models.Cabinet;
import com.clinitalPlatform.models.CabinetMedecinsSpace;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.payload.request.CabinetMedecinsSpaceRequest;

import java.util.List;

public interface MedecinCabinetService {
    
     public CabinetMedecinsSpace addCabinetMedecinsSpace(CabinetMedecinsSpaceRequest medecinNetwor,Cabinet cabinet,Medecin medecin) throws Exception;
     public void  deleteCabinetMedecins(Long idcab) throws Exception;

     List<Medecin> getAllMedecinsByCabinetName(String nomCabinet)throws Exception;
}
