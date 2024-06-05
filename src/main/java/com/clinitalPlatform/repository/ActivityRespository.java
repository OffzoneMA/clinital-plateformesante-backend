package com.clinitalPlatform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.clinitalPlatform.models.LogActivityUser;

@Repository
public interface ActivityRespository extends JpaRepository<LogActivityUser, Long> {

    public List<LogActivityUser> findAll();

    @Query("from LogActivityUser l  where l.user.id = ?1")
	public List<LogActivityUser> findActivityByIdUsers(Long user_id);

    @Query(value ="SELECT * FROM LogActivityUser WHERE id = :id",nativeQuery = true)
	LogActivityUser getById(Long id);

    public LogActivityUser findActivityById(Long id);
    
}
