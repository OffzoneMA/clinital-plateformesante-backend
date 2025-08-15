package com.clinitalPlatform.repository;

import com.clinitalPlatform.enums.InvitationStatus;
import com.clinitalPlatform.models.InvitationEquipe;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InvitationEquipeRepository extends JpaRepository<InvitationEquipe, Long> {
    Optional<InvitationEquipe> findByToken(String token);


    @Query(value = "SELECT * FROM invitations_equipe WHERE cabinet_id = ?1", nativeQuery = true)
    List<InvitationEquipe> findByCabinetId(Long cabinetId);

    @Query("SELECT i FROM InvitationEquipe i WHERE i.email = :email AND i.cabinet.id_cabinet = :cabinetId AND i.status = :status")
    InvitationEquipe findByEmailAndCabinetIdAndStatus(
            @Param("email") String email,
            @Param("cabinetId") Long cabinetId,
            @Param("status") InvitationStatus invitationStatus
    );

    @Modifying
    @Query("DELETE FROM InvitationEquipe i WHERE i.id = :id")
    void deleteById(@NotNull Long id);

    @Query("SELECT i FROM InvitationEquipe i WHERE i.id = :id")
    InvitationEquipe getById(Long id);

}
