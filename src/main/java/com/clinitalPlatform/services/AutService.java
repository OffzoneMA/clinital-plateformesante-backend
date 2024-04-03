package com.clinitalPlatform.services;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import com.clinitalPlatform.models.Patient;
import com.clinitalPlatform.repository.PasswordResetTokenRepository;
import com.clinitalPlatform.repository.PatientRepository;
import com.clinitalPlatform.security.jwt.PasswordResetToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinitalPlatform.models.User;

import com.clinitalPlatform.repository.ConfirmationTokenRepository;
import com.clinitalPlatform.repository.UserRepository;
import com.clinitalPlatform.security.jwt.ConfirmationToken;

@Transactional
@Service
public class AutService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ConfirmationTokenRepository confirmationTokenRepository;
	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private PasswordResetTokenRepository passwordResetTokenRepository;
    
	public void updateLastLoginDate(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UsernameNotFoundException("User Not Found with id: " + userId));

		user.setLastLogin(new Date());

		userRepository.save(user);
	}
	 public ConfirmationToken createToken(User user) {
	        ConfirmationToken confirmationToken = new ConfirmationToken(user);
	        return confirmationTokenRepository.save(confirmationToken);
	 } //creation du token de confirmation


	public PasswordResetToken createRestetToken(User user){
		PasswordResetToken passwordResetToken=new PasswordResetToken(user);
		return passwordResetTokenRepository.save(passwordResetToken);
	}//Creation du token de reinitialisation du password
	 
	 public User save(User user){
	       return userRepository.save(user);
	  }

	 public ConfirmationToken findByConfirmationToken(String token) {
	        return confirmationTokenRepository.findByConfirmationToken(token);
	    }

		public PasswordResetToken findByResetToken(String resetToken){
		return passwordResetTokenRepository.findByResetToken(resetToken);
		} //faire une recherche via le token de reinitialisation

	 public ConfirmationToken getConfirmationTokenByUserId(Long userid){
		return confirmationTokenRepository.getConfirmationTokenByUserId(userid);
	 } // Obtenir le Token de confirmation par user id

	public PasswordResetToken findByUserId(Long userId ){
		return passwordResetTokenRepository.findByUserId(userId);
	} //TOKEN DE REINITIALISATION du password PAR USER ID

	public Boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}


	public User findByEmail(String email){
		return userRepository.findByEmail(email);
	 }

	 //Les methodes qui servirons à un patient de reinitialiser son mot de passe en cas d'oublié
	public Optional<Patient> findByPatientEmail(String patientEmail) {
		return patientRepository.findByPatientEmail(patientEmail);
	}

	public String getDateOfBirthByEmail(String patientEmail) {
		Optional<Patient> patientOptional = patientRepository.findByPatientEmail(patientEmail);
		if (patientOptional.isPresent()) {
			Patient patient = patientOptional.get();
			Date dateNaissance = patient.getDateNaissance();
			if (dateNaissance != null) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
				return dateFormat.format(dateNaissance);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public String getPlaceOfBirthByEmail(String patientEmail) {
		Optional<Patient> patientOptional = patientRepository.findByPatientEmail(patientEmail);
		if (patientOptional.isPresent()) {
			Patient patient = patientOptional.get();
			return patient.getPlaceOfBirth();
		} else {
			return null;
		}
	}


}

