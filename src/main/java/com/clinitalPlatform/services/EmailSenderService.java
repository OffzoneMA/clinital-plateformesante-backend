package com.clinitalPlatform.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
		mailMessage.setText("Bonjour nous vous souhaitons la bienvenue sur la plateforme Clinital pour confirmer votre compte"
				+ ", merci de cliquer sur le lien: "
				+ BaseUrl+"/api/auth/confirmaccount?token=" + confirmationToken
				+ "   Note: Ce lien va expirer après 10 minutes.");
		javaMailSender.send(mailMessage);
		LOGGER.info("A New Account has been Created, token activationis sent");

		}catch(Exception e){
			LOGGER.error("Error while sending email : {}",e);
			System.out.println(2);
		}
		
	}
//newlink email
	public void sendMailConfirmationNewlink(String userEmail, String newLink) {
		final String BaseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
		System.out.println("this is the URL Root :"+BaseUrl);
		try {
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(userEmail);
			mailMessage.setFrom("clinitalcontact@gmail.com");
			mailMessage.setSubject("Activation du compte Clinital!");
			mailMessage.setText("Bonjour, nous vous souhaitons la bienvenue sur la plateforme Clinital pour confirmer votre compte."
					+ " Merci de cliquer sur le lien suivant pour activer votre compte : "

					+ BaseUrl+"/api/auth/confirmaccount?token="+ newLink + ". Notez que ce lien expirera après 10 minutes.");
			javaMailSender.send(mailMessage);
			LOGGER.info("Un nouveau lien d'activation de compte a été envoyé à l'utilisateur: {}", userEmail);
		} catch (Exception e) {
			LOGGER.error("Erreur lors de l'envoi de l'e-mail de confirmation : {}", e);
		}
	}
	//end new link email

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
			);
				
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
				
				+ "\r\n"
				+ "Password provisoire: "+pw);
				
		javaMailSender.send(mailMessage);
		LOGGER.info("A New Demande has been created ");
		System.out.println("Email sent");
	}
	public void sendMailConfirmationCode(String userEmail, String confirmationcode) {
		
	    try {
	        String message = "Bonjour,\n\n"
	                + "Vous avez demandé la suppression de votre compte sur la plateforme Clinital.\n\n"
	                + "Si vous souhaitez toujours supprimer votre compte, voila  le code de confirmation "
	                + "\n\n"
	                + confirmationcode + "\n\n"
	                + "vous pouvez ignorer cet email.";

	        SimpleMailMessage mailMessage = new SimpleMailMessage();
	        mailMessage.setTo(userEmail);
	        mailMessage.setFrom("clinitalcontact@gmail.com");
	        mailMessage.setSubject("Code de confirmation du suppression   de   votre compte clinital!");
	        mailMessage.setText(message);

	        javaMailSender.send(mailMessage);
	        LOGGER.info("Un email de  code de confirmation a été envoyé à l'adresse : {}", userEmail);
	    } catch (Exception e) {
	        LOGGER.error("Erreur lors de l'envoi de l'e-mail de confirmation : {}", e);
	    }
	}
public void sendMailChangePassword(String userEmail) {
		
	    try {
	    	 String message = "Bonjour,\n\n"
	                    + "Votre mot de passe a été modifié avec succès sur la plateforme Clinital.\n\n"
	                    + "Si vous n'avez pas effectué cette modification, veuillez contacter notre équipe de support.";

	        SimpleMailMessage mailMessage = new SimpleMailMessage();
	        mailMessage.setTo(userEmail);
	        mailMessage.setFrom("clinitalcontact@gmail.com");
	        mailMessage.setSubject("Code de confirmation du suppression   de   votre compte clinital!");
	        mailMessage.setText(message);

	        javaMailSender.send(mailMessage);
            LOGGER.info("Un email de notification de changement de mot de passe a été envoyé à l'adresse : {}", userEmail);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'envoi de l'e-mail de notification de changement de mot de passe : {}", e);
        }
    }
}