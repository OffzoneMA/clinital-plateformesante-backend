package com.clinitalPlatform.repository;

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


	// Dans votre interface UserRepository
	@Query(value = "SELECT u.email_verified FROM users u WHERE u.email = ?1", nativeQuery = true)
	Boolean findEmailVerifiedByEmail(String email);




}
