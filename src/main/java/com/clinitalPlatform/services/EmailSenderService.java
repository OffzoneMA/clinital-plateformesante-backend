package com.clinitalPlatform.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.clinitalPlatform.models.Demande;

@Transactional
@Service
public class EmailSenderService {

	@Autowired
	private JavaMailSender javaMailSender;

	private final Logger LOGGER=LoggerFactory.getLogger(getClass());
	
	public void sendMailConfirmation(String userEmail, String confirmationToken) {
		final String BaseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
		System.out.println("this is the URL Root :"+BaseUrl);
		try{
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		System.out.println("this is the URL Root :"+userEmail);
		mailMessage.setTo(userEmail);
		mailMessage.setFrom("clinitalcontact@gmail.com");
		mailMessage.setSubject("Activation du compte clinital!");
		mailMessage.setText("Bonjour nous vous souhaiton la bienvenue sur la plateforme Clinital pour confirmer votre compte"
				+ ", merci de cliquer sur le lien: "
				+ BaseUrl+"/api/auth/confirmaccount?token=" + confirmationToken
				+ "   Note: le lien va expirer après 10 minutes.");
		javaMailSender.send(mailMessage);
		LOGGER.info("A New Account has been Created, token activationis sent");

		}catch(Exception e){
			LOGGER.error("Error while sending email : {}",e);
			System.out.println(2);
		}
		
	}

	public void sendMailDemande(Demande demande) {
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(demande.getMail());
		mailMessage.setFrom("clinitalcontact@gmail.com");
		mailMessage.setSubject("Activation de la partie pro pour le médecin : "+demande.getNom_med());
		mailMessage.setText("Le Médecin :"+demande.getNom_med()+" veut accéder à la partie pro"
				+ "\n leurs cordonnées :  \n\n"
				+ "Medecin: \r\n"
				+ "Nom: "+demande.getNom_med()+"\n"
				+ "\r\n"
				+ "Prenom: "+demande.getPrenom_med()+"\n"
				+ "\r\n"
				+ "Matricule: "+demande.getMatricule()+"\n"
				+ "\r\n"
				+ "Spécialité: "+demande.getSpecialite()+"\n"
				+ "\r\n"
				+ "INPE: "+demande.getInpe()+"\n"
				+ "\r\n"
				+ "Cabinet : \r\n"+demande.getNom_cab() + "\r\n"
				+ "Nom : "+demande.getNom_cab()+"\n"
				+ "\r\n"
				+ "Adresse: "+demande.getAdresse()+"\n"
				+ "\r\n"
				+ "Code postale: "+demande.getCode_postal());
				
		javaMailSender.send(mailMessage);
		LOGGER.info("A New Pro Account has been created ");
		System.out.println("Email sent");
	}
	
	public void sendMailDemandeValidation(Demande demande,String pw ) {
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(demande.getMail());
		mailMessage.setFrom("clinitalcontact@gmail.com");
		mailMessage.setSubject("Activation de la partie pro pour le médecin :"+demande.getNom_med());
		mailMessage.setText("Le Médecin :"+demande.getNom_med()+"veut accéder à la partie pro"
				+ "\n leurs cordonnées :  \n"
				+ "Medecin:\r\n"
				+ "Nom:"+demande.getNom_med()+"\n"
				+ "\r\n"
				+ "Prenom:"+demande.getPrenom_med()+"\n"
				+ "\r\n"
				+ "\r\n"
				//+ "passwaord provesoire:"+user.getPassword()+"\n"
				+ "Matricule:"+demande.getMatricule()+"\n"
				+ "\r\n"
				+ "Spécialité"+demande.getSpecialite()+"\n"
				+ "\r\n"
				+ "INPE:"+demande.getInpe()+"\n"
				+ "\r\n"
				+ "Cabinet : \r\n"
				+ "\r\n"
				+ "Nom: "+demande.getNom_cab()+"\n"
				+ "\r\n"
				+ "Adresse: "+demande.getAdresse()+"\n"
				+ "\r\n"
				+ "Code postale: "+demande.getCode_postal()
				+ "\r\n"
				+ "Password provisoire: "+pw);
				
		javaMailSender.send(mailMessage);
		LOGGER.info("A New Demande has been created ");
		System.out.println("Email sent");
	}

}