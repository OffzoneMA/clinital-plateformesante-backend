package com.clinitalPlatform.services;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinitalPlatform.models.Ordonnance;
import com.clinitalPlatform.payload.request.OrdonnanceRequest;
import com.clinitalPlatform.models.DossierMedical;
import com.clinitalPlatform.models.Rendezvous;
import com.clinitalPlatform.repository.RdvRepository;
import com.clinitalPlatform.repository.DossierMedicalRepository;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.dto.OrdonnanceDTO;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.repository.OrdonnanceRepository;
import com.clinitalPlatform.services.interfaces.OrdonnanceServices;
import com.clinitalPlatform.util.ClinitalModelMapper;

@Transactional
@Service
public class OrdonnanceServiceImpl implements OrdonnanceServices{
	
    @Autowired
    private OrdonnanceRepository ordorepo;
    @Autowired
    private ClinitalModelMapper mapper;
    
    @Autowired
    private RdvRepository rdvRepository;
 
    @Autowired
    private DossierMedicalRepository dossierMedicalRepository;
   
    @Override
    public List<OrdonnanceDTO> findAllByMed(Medecin med) throws Exception {
        try {
            List<OrdonnanceDTO> allOrdonnances=ordorepo.findAllByIdMed(med.getId()).stream().map(consul->mapper.map(consul,OrdonnanceDTO.class)).collect(Collectors.toList());
            return allOrdonnances;  
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
     
    }

	@Override
    public OrdonnanceDTO findById(Long id,Medecin med) throws Exception {
        try {
            Ordonnance ordonnance=ordorepo.findIdandIdMedecin(id,med.getId()).orElseThrow(()->new Exception("No matching found for this Ordonnance"));
            return mapper.map(ordonnance,OrdonnanceDTO.class);  
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
     
    }

	@Override
    public Boolean deleteById(Ordonnance ordonnance) throws Exception {
        try {
            Boolean IsDeleted= OrdonnanceRepository.deleteOrdonnance(ordonnance.getId_ordon())?true:false;
            return IsDeleted;  
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
	
    @Override
    public List<OrdonnanceDTO> findAll() throws Exception {
        try {
            List<OrdonnanceDTO> allOrdonnances=ordorepo.findAll().stream().map(consul->mapper.map(consul,OrdonnanceDTO.class)).collect(Collectors.toList());
            return allOrdonnances;  
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
     
    }

	@Override
	public Ordonnance create(OrdonnanceRequest consultationRequest, Medecin med) throws Exception {
		 try {
	            DossierMedical dossier=dossierMedicalRepository.findById(consultationRequest.getDossier()).orElseThrow(()->new Exception("No matching found for this Dossier"));
	            Rendezvous rdv=rdvRepository.findById(consultationRequest.getRendezvous()).orElseThrow(()->new Exception("No matching found for this Rendezvous"));
	            Ordonnance ordonnance=new Ordonnance();
	            ordonnance.setDate(LocalDate.now());
	            ordonnance.setDetails(consultationRequest.getDetails());
	            ordonnance.setMedecin(med);
	            ordonnance.setDossier(dossier);
	            ordonnance.setRendezvous(rdv);
	            ordorepo.save(ordonnance);
	            return ordonnance;
	            
	            } catch (Exception e) {
	            throw new Exception(e.getMessage());
	            }    
	}

	@Override
	public OrdonnanceDTO update(OrdonnanceRequest req, Medecin med) throws Exception {
		 try {
	            Ordonnance ordonnance=ordorepo.findById(req.getId_ordonnance()).orElseThrow(()->new Exception("No matching found for this Ordonnance"));
	            DossierMedical dossier=dossierMedicalRepository.findById(req.getDossier()).orElseThrow(()->new Exception("No matching found for this Dossier"));
	            Rendezvous rdv=rdvRepository.findById(req.getRendezvous()).orElseThrow(()->new Exception("No matching found for this Rendezvous"));
	            
	            ordonnance.setDate(req.getDate());
	            ordonnance.setDetails(req.getDetails());
	            ordonnance.setMedecin(med);
	            ordonnance.setDossier(dossier);
	            ordonnance.setRendezvous(rdv);
	            ordorepo.save(ordonnance);
	            return mapper.map(ordonnance,OrdonnanceDTO.class);
	               
	            } catch (Exception e) {
	            throw new Exception(e.getMessage());
	            }	    
	}
	
	@Override
	 public ResponseEntity<?> findByIdMedandDossierId(Medecin med,Long iddoss,Long idordo) throws Exception {
	        try {
	            
			DossierMedical dossier = dossierMedicalRepository.findById(iddoss).orElseThrow(()->new Exception("NO such Folder exist"));

	        Ordonnance ordonnance=ordorepo.findIdandIdDossier(idordo, iddoss).orElseThrow(()->new Exception("No Matchng found for this Id ordonnace in this patient folder"));
			// check if the folder is already shared.
			Boolean isDossshared=med.getMeddossiers().stream().filter(doss->doss.getId_dossier()==dossier.getId_dossier()).findFirst().isPresent();

	        if(isDossshared){
	           return ResponseEntity.ok(ordonnance); 
	        }else{

	            return ResponseEntity.ok(new ApiResponse(false, "you Cant access to this resource"));
			}
   
	        } catch (Exception e) {
	            throw new Exception(e.getMessage());
	        }
	     
	    }
   
}
