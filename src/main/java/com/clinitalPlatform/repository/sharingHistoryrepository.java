package com.clinitalPlatform.repository;

import com.clinital.models.Medecin;
import com.clinital.models.SharingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface sharingHistoryrepository extends JpaRepository<SharingHistory, Long> {

    @Modifying
	@Query(value = "DELETE FROM sharing_history WHERE id = :idshare", nativeQuery = true)
	public static boolean Deletsharehstory(@Param("idshare") Long idshare) throws Exception{
        return true;
    };

    @Query(value = "select * from sharing_history where id_med=?1 and id_patient=?2 ", nativeQuery = true)
    public List<SharingHistory> findAllSharingHistoryByMedecinIdAndPatientId(Long id_medecin,Long id_patient) throws Exception;

    @Query(value = "select * from sharing_history where id_patient=?1", nativeQuery = true)
    public List<SharingHistory> findAllSharingHistoryByPatientId(Long id_patient) throws Exception;

    @Query(value = "select m.* from medecins m, sharing_history s where m.id=s.id_med and  id_med=?1 and id_patient=?2", nativeQuery = true)
    public List<Medecin> findAllSharingHistoryByMedecinId(Long id_med,long id_patient) throws Exception;

    @Query(value = "SELECT s.* FROM `sharing_history` s WHERE s.id_med=?1 and s.id_user=?2", nativeQuery = true)
    public List<SharingHistory> findAllSharingHistoryByMedecinIdAndUserId(Long id_medecin,Long id_User) throws Exception;

    @Query(value = "SELECT s.* FROM `sharing_history` s WHERE s.id_user=?1", nativeQuery = true)
    public List<SharingHistory> findAllSharingHistoryByUserId(Long user_id) throws Exception;

    @Query(value = "SELECT s.* FROM `sharing_history` s WHERE s.id_dossier=?1", nativeQuery = true)
    public List<SharingHistory> findAllSharingHistoryByDossierId(Long id_dossier) throws Exception;
    
}
