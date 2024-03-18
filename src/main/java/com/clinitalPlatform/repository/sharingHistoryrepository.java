package com.clinitalPlatform.repository;

import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.SharingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;

public interface sharingHistoryrepository extends JpaRepository<SharingHistory, Long> {


        @Query(value = "select * from sharing_history where id_med=?1 and id_patient=?2 ", nativeQuery = true)
        public List<SharingHistory> findAllSharingHistoryByMedecinIdAndPatientId(Long id_medecin, Long id_patient) throws Exception;

        @Query(value = "select * from sharing_history where id_patient=?1", nativeQuery = true)
        public List<SharingHistory> findAllSharingHistoryByPatientId(Long id_patient) throws Exception;

        @Query(value = "select * from sharing_history where id_med=?1", nativeQuery = true)
        public List<Medecin> findAllSharingHistoryByMedecinId(Long id_med) throws Exception;

        @Query(value = "SELECT s.* FROM `sharing_history` s WHERE s.id_med=?1 and s.id_user=?2", nativeQuery = true)
        public List<SharingHistory> findAllSharingHistoryByMedecinIdAndUserId(Long id_medecin, Long id_User) throws Exception;

        @Query(value = "SELECT s.* FROM `sharing_history` s WHERE s.id_user=?1", nativeQuery = true)
        public List<SharingHistory> findAllSharingHistoryByUserId(Long user_id) throws Exception;

        @Query(value = "SELECT * FROM sharing_history WHERE id_dossier=?1", nativeQuery = true)
        public List<SharingHistory> findAllSharingHistoryByDossierId(Long id_dossier) throws Exception;

}
