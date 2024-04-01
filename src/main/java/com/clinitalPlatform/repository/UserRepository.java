package com.clinitalPlatform.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import org.springframework.data.jpa.repository.Query;
=======
>>>>>>> 99085ea3f9b1233061d1e0ed0b85ffba46361418
import org.springframework.stereotype.Repository;

import com.clinitalPlatform.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	
	Boolean existsByEmail(String email);
	Optional<User> findUserByEmail(String email);
	User findByEmail(String email);

<<<<<<< HEAD

	@Query(value = "SELECT u.email_verified FROM users u WHERE u.email = ?1", nativeQuery = true)
	Boolean findEmailVerifiedByEmail(String email);

	//@Query(value = "SELECT u.is_enabled FROM users u WHERE u.email = ?1 AND u.is_enabled=0 AND u.email_verified=1",nativeQuery = true)
	//Boolean findIsEnabledByEmail(String email);
	/*@Query(value = "SELECT CASE WHEN u.is_enabled = 0 THEN FALSE ELSE TRUE END AS is_enabled FROM users u WHERE u.email = ?1", nativeQuery = true)
	Optional<Boolean> findIsEnabledByEmail(String email);*/
	//SELECT u.is_enabled,u.email,u.email_verified FROM users u WHERE u.is_enabled=0 AND u.email_verified=1;

	@Query(value = "SELECT u.is_enabled FROM users u WHERE u.email = ?1", nativeQuery = true)
	Optional<Boolean> findIsEnabledByEmail(String email);


=======
>>>>>>> 99085ea3f9b1233061d1e0ed0b85ffba46361418
}
