package com.clinitalPlatform.services;

import com.clinitalPlatform.repository.CabinetRepository;
import com.clinitalPlatform.repository.MedecinRepository;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinitalPlatform.models.Cabinet;
import com.clinitalPlatform.models.CabinetMedecinsSpace;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.payload.request.CabinetMedecinsSpaceRequest;
import com.clinitalPlatform.repository.CabinetMedecinRepository;
import com.clinitalPlatform.services.interfaces.MedecinCabinetService;

import javax.ws.rs.InternalServerErrorException;
import java.util.List;

import static org.hibernate.sql.ast.SqlTreeCreationLogger.LOGGER;


@Service
@Transactional
public class CabinetMedecinServiceImpl implements MedecinCabinetService {

    @Autowired
    private CabinetMedecinRepository cabmedrepo;

    @Autowired
    private CabinetRepository cabinetRepository;

    @Autowired
    private MedecinRepository medecinRepository;

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

    //Recuperer la liste de medecins d'un cabinet

    @Override
    public List<Medecin> getAllMedecinsByCabinetName(String nomCabinet) {
        // Récupérer le cabinet par son nom
        List<Cabinet> cabinets = cabinetRepository.findByNomContainingIgnoreCase(nomCabinet);
        if (cabinets.isEmpty()) {
            System.out.println("Cabinet not found with name: " + nomCabinet);
            throw new RuntimeException("Cabinet not found with name: " + nomCabinet);

        }
        // Récupérer les médecins associés à ce cabinet
        List<Medecin> medecins = medecinRepository.getAllMedecinsByCabinetName(nomCabinet);

        return medecins;
    }




}
