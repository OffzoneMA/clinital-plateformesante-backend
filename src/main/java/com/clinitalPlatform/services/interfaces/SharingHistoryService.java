package com.clinitalPlatform.services.interfaces;

import com.clinitalPlatform.dto.SharingHistoryDTO;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.SharingHistory;
import com.clinitalPlatform.payload.request.SharingHistoryRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface SharingHistoryService {

    public SharingHistory addSharingHistory(SharingHistoryRequest SharingHistory) throws Exception;

    public ResponseEntity<SharingHistory> updateSharingHistory(SharingHistoryRequest SharingHistory) throws Exception;

    public String deleteSharingHistoryById(Long id) throws Exception;

    public List<SharingHistory> findAllSharingHistoryByMedecinIdAndPatientId(Long id_medecin, Long id_patient) throws Exception;
    public List<SharingHistory> findAllSharingHistoryByPatientId(Long id_patient) throws Exception;
    public List<Medecin> findAllSharingHistoryByMedecinId(Long id_med) throws Exception;
    public List<SharingHistory> findAllSharingHistoryByMedecinIdAndUserId(Long id_medecin) throws Exception;
    public ResponseEntity<?> findAllSharingHistoryByUserId(Long user_id) throws Exception;
    public List<SharingHistory> findAllSharingHistoryByDocId(Long id_document) throws Exception;

    public SharingHistory findSharingHistoryById(Long id_share) throws Exception;

}
