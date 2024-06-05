package com.clinitalPlatform.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinitalPlatform.models.Cabinet;
import com.clinitalPlatform.models.CabinetMedecinsSpace;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.payload.request.CabinetMedecinsSpaceRequest;
import com.clinitalPlatform.repository.CabinetMedecinRepository;
import com.clinitalPlatform.services.interfaces.MedecinCabinetService;

@Service
@Transactional
public class CabinetMedecinServiceImpl implements MedecinCabinetService {

    @Autowired
    private CabinetMedecinRepository cabmedrepo;

    @Override
    public CabinetMedecinsSpace addCabinetMedecinsSpace(CabinetMedecinsSpaceRequest medecincabinetreq, Cabinet cabinet,
            Medecin medecin) throws Exception {
        
        CabinetMedecinsSpace CabMed=new CabinetMedecinsSpace(medecin,cabinet,medecincabinetreq.getStatus());
        cabmedrepo.save(CabMed);

        return CabMed;
    }
    
    @Override
    public void deleteCabinetMedecins(Long idcab) throws Exception {
        cabmedrepo.DeleteCabinetbyID(idcab);
        
    }


}
