package com.clinitalPlatform.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.clinitalPlatform.models.Demande;

import java.text.SimpleDateFormat;
import java.util.Date;

@Transactional
@Service
public class EmailSenderService {

	@Autowired
	private JavaMailSender javaMailSender;

	private final Logger LOGGER=LoggerFactory.getLogger(getClass());
	@Value("${front.url}")
	private String frontUrl;

	private final String SUPPORT_EMAIL = "roukeyaassouma@gmail.com";

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
	//end new link emai
	
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

	public void sendMail(String userEmail, String confirmationToken) {

		try{
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			final String BaseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
			System.out.println("this is the URL Root :"+BaseUrl);
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

	//Email de reinistialisation du password

	public void sendResetPasswordMail(String userEmail, String resetToken) {
		try {
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			String resetPasswordUrl =frontUrl+ "/login/reinitialize-password?reset=" + resetToken;

			mailMessage.setTo(userEmail);
			mailMessage.setFrom("clinitalcontact@gmail.com");
			mailMessage.setSubject("Réinitialisation de votre mot de passe");
			mailMessage.setText("Bonjour,\n\n"
					+ "Vous avez demandé la réinitialisation de votre mot de passe pour votre compte Clinital.\n\n"
					+ "Veuillez cliquer sur le lien ci-dessous pour réinitialiser votre mot de passe :\n"
					+ resetPasswordUrl
					+ "\n\nCe lien expirera dans 10 minutes.\n\n"
					+ "Cordialement,\nVotre équipe Clinital");
			javaMailSender.send(mailMessage);
			LOGGER.info("E-mail de réinitialisation du mot de passe envoyé à {}", userEmail);
		} catch (Exception e) {
			LOGGER.error("Erreur lors de l'envoi de l'e-mail de réinitialisation du mot de passe : {}", e);
		}
	}

    public void sendEmailChangeNotification(String oldEmail, String newEmail) {
        final String BaseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();

            // E-mail vers l'ancienne adresse
            mailMessage.setTo(oldEmail);
            mailMessage.setFrom("clinitalcontact@gmail.com");
            mailMessage.setSubject("Notification de changement d'adresse e-mail");
            mailMessage.setText("Bonjour,\n\n"
                    + "Votre adresse e-mail associée à votre compte Clinital a été modifiée avec succès.\n"
                    + "Nouvelle adresse e-mail : " + newEmail + "\n\n"
                    + "Si vous n'êtes pas à l'origine de cette modification, veuillez nous contacter immédiatement.\n\n"
                    + "Cordialement,\nL'équipe Clinital");
            javaMailSender.send(mailMessage);

            // E-mail vers la nouvelle adresse
            //mailMessage.setTo(newEmail);
            /*mailMessage.setText("Bonjour,\n\n"
                    + "Votre adresse e-mail a été mise à jour avec succès dans notre système Clinital. "
                    + "Si vous n'avez pas demandé ce changement, veuillez nous contacter immédiatement.\n\n"
                    + "Cordialement,\nL'équipe Clinital");
            javaMailSender.send(mailMessage);*/

            LOGGER.info("Notification de changement d'adresse e-mail envoyée à l'utilisateur (Ancienne : {}, Nouvelle : {})", oldEmail, newEmail);

        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'envoi de l'e-mail de notification de changement : {}", e.getMessage());
            System.out.println("Erreur lors de l'envoi de l'e-mail");
        }
    }


	public void sendProcheDeletionNotification(String userEmail, String procheName) {
		try {
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(userEmail);
			mailMessage.setFrom("clinitalcontact@gmail.com");
			mailMessage.setSubject("Confirmation de suppression d'un proche - Clinital");

			String message = String.format(
					"Bonjour,\n\n" +
							"Nous vous confirmons la suppression du proche '%s' de votre compte Clinital.\n\n" +
							"Détails de l'opération :\n" +
							"- Date de suppression : %s\n" +
							"- Proche supprimé : %s\n" +
							//"- Compte concerné : %s\n\n" +
							"Important :\n" +
							"- Cette action est irréversible\n" +
							"- Toutes les données associées à ce proche ont été supprimées\n" +
							//"- Les rendez-vous futurs liés à ce proche ont été annulés\n\n" +
							"Si vous n'êtes pas à l'origine de cette suppression ou si vous pensez qu'il s'agit d'une erreur, " +
							"veuillez nous contacter immédiatement :\n" +
							"- Par email : support@clinital.com\n" +
							//"- Par téléphone : +XXX XXX XXX\n\n" +
							//"Pour des raisons de sécurité, nous vous conseillons de vérifier régulièrement " +
							//"les activités de votre compte dans la section 'Historique des activités'.\n\n" +
							"Cordialement,\n" +
							"L'équipe Clinital",
					procheName,
					new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()),
					procheName,
					userEmail
			);

			mailMessage.setText(message);

			javaMailSender.send(mailMessage);
			LOGGER.info("Email de notification de suppression du proche '{}' envoyé à : {}", procheName, userEmail);

		} catch (MailException e) {
			LOGGER.error("Erreur lors de l'envoi de l'email (Proche: {}, Email: {}) : {}",
					procheName, userEmail, e.getMessage());
			throw new RuntimeException("Impossible d'envoyer l'email de confirmation de suppression", e);
		} catch (Exception e) {
			LOGGER.error("Erreur inattendue lors de l'envoi de l'email de notification : {}", e.getMessage());
			throw new RuntimeException("Une erreur inattendue est survenue lors de l'envoi de l'email", e);
		}
	}

	public void sendContactForm(String userEmail, String prenom, String nom, String telephone, String message, String userType) {
		try {
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(SUPPORT_EMAIL);
			mailMessage.setFrom(userEmail);
			mailMessage.setSubject("Nouveau message de contact de " + prenom + " " + nom + " (" + userType + ")");
			mailMessage.setText(
					"Type d'utilisateur: " + userType + "\n" +
							"Nom: " + nom + "\n" +
							"Prénom: " + prenom + "\n" +
							"Email: " + userEmail + "\n" +
							"Téléphone: " + (telephone.isEmpty() ? "Non fourni" : telephone) + "\n\n" +
							"Message:\n" + message
			);
			javaMailSender.send(mailMessage);
			LOGGER.info("Le formulaire de contact a été envoyé avec succès au support.");

		} catch (Exception e) {
			LOGGER.error("Erreur lors de l'envoi du formulaire de contact : {}", e.getMessage());
		}
	}

	// Envoi de confirmation de réception au user
	public void sendContactConfirmation(String userEmail, String prenom) {
		try {
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(userEmail);
			mailMessage.setFrom("clinitalcontact@gmail.com");
			mailMessage.setSubject("Confirmation de votre demande de contact");
			mailMessage.setText(
					"Bonjour " + prenom + ",\n\n" +
							"Nous avons bien reçu votre message et nous vous répondrons dans les plus brefs délais.\n\n" +
							"Merci de nous avoir contactés.\n\n" +
							"L'équipe Clinital"
			);
			javaMailSender.send(mailMessage);
			LOGGER.info("Email de confirmation envoyé à l'utilisateur : " + userEmail);

		} catch (Exception e) {
			LOGGER.error("Erreur lors de l'envoi de l'email de confirmation : {}", e.getMessage());
		}
	}

}
