package com.clinitalPlatform.services;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinitalPlatform.models.Cabinet;
import com.clinitalPlatform.models.DocumentsCabinet;
import com.clinitalPlatform.payload.request.DocumentsCabinetRequest;
import com.clinitalPlatform.repository.CabinetRepository;
import com.clinitalPlatform.repository.DocumentsCabinetRepository;
@Transactional
@Service
public class DocumentsCabinetServices {

    @Autowired
    private CabinetRepository cabrepo;

    @Autowired
    private DocumentsCabinetRepository docrepo;
    
    public DocumentsCabinet create(DocumentsCabinetRequest docreq) throws Exception {
        Cabinet cabinet = cabrepo.findById(docreq.getId_cabinet()).orElseThrow(()->new Exception("No Matching Cabinet found"));
			
			DocumentsCabinet documentEntity = new DocumentsCabinet();
			documentEntity.setType_doc(docreq.getType());
			documentEntity.setDate_ajout_doc(LocalDate.now());
			documentEntity.setCabinet(cabinet);
            docrepo.save(documentEntity);
        return documentEntity;
    }

    
 
}
