package com.clinitalPlatform.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.clinitalPlatform.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	
	Boolean existsByEmail(String email);
	Optional<User> findUserByEmail(String email);
	User findByEmail(String email);

}
