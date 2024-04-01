package com.clinitalPlatform.util;

import com.clinitalPlatform.dto.SharingHistoryDTO;
import com.clinitalPlatform.models.SharingHistory;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Service;

@Service
public class ClinitalModelMapper extends ModelMapper {

    /*public ClinitalModelMapper() {
        // Configure the mapping for the patient property
        addMappings(new PropertyMap<SharingHistory, SharingHistoryDTO>() {
            protected void configure() {
                map().setPatientId(source.getPatient().getId()); // Assuming you want to map the patient's ID
            }
        });
    }*/


}

