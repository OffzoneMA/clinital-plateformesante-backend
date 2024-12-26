package com.clinitalPlatform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.clinitalPlatform.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	
	Boolean existsByEmail(String email);
  
	Optional<User> findUserByEmail(String email);
  
	User findByEmail(String email);

	@Query(value ="SELECT * FROM users WHERE id = :id",nativeQuery = true)
	User getById(Long id);

	@Query(value = "SELECT u.email_verified FROM users u WHERE u.email = ?1", nativeQuery = true)
	Boolean findEmailVerifiedByEmail(String email);

	@Query(value = "SELECT u.is_enabled FROM users u WHERE u.email = ?1", nativeQuery = true)
	Optional<Boolean> findIsEnabledByEmail(String email);

	List<User> findAllByEmail(String email);
}
