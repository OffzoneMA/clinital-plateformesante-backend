package com.clinitalPlatform.controllers;

import java.text.SimpleDateFormat;
import java.util.*;


import com.clinitalPlatform.models.*;
import com.clinitalPlatform.enums.ERole;
import com.clinitalPlatform.payload.request.*;
import com.clinitalPlatform.payload.response.MessageResponse;
import com.clinitalPlatform.repository.MedecinRepository;
import com.clinitalPlatform.repository.PasswordResetTokenRepository;
import com.clinitalPlatform.security.jwt.PasswordResetToken;
import com.clinitalPlatform.security.services.UserDetailsServiceImpl;
import com.clinitalPlatform.services.*;
import com.clinitalPlatform.services.interfaces.MedecinService;
import com.clinitalPlatform.services.interfaces.SecretaireService;
import com.clinitalPlatform.util.ApiError;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clinitalPlatform.util.GlobalVariables;
import com.clinitalPlatform.exception.BadRequestException;
import com.clinitalPlatform.payload.response.ApiResponse;
import com.clinitalPlatform.payload.response.JwtResponse;
import com.clinitalPlatform.repository.UserRepository;
import com.clinitalPlatform.security.jwt.ConfirmationToken;
import com.clinitalPlatform.security.jwt.JwtService;
import com.clinitalPlatform.security.services.UserDetailsImpl;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;



import javax.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${base.url}")
    private String baseUrl;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    ActivityServices activityServices;

    @Autowired
    UserService userServices;

    @Autowired
    AutService autService;

    @Autowired
    private GlobalVariables globalVariables;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    EmailSenderService emailSenderService;
    @Autowired
    PasswordEncoder encoder;

    @Autowired
    MedecinService medecinSevice;

    @Autowired
    private MedecinRepository medecinRepository;

    @Autowired
    SecretaireService secretaireService;

	@Autowired
	private DemandeServiceImpl demandeService;

    @Autowired
    private DocumentsCabinetServices documentsCabinetServices;

    @Autowired
    private SubscriptionService subscriptionService;
	
    @Autowired
    UserRepository userRepository;
	
	
    //Jounalisation
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private MedecinServiceImpl medecinServiceImpl;


    //CONNEXION--------------------------------------------------
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateAndGetToken(@RequestBody LoginRequest loginRequest) {
        try {
            // Vérifier si le compte existe (via le mail)
            UserDetails userDetail = userDetailsService.loadUserByUsername(loginRequest.getEmail());

            // Vérifier si le compte est activé
            User useractif = userServices.findByEmail(loginRequest.getEmail());
            if (useractif != null && !useractif.getEmailVerified()) {
                LOGGER.info("Email is not verified");
                return ResponseEntity.ok(new ApiResponse(false, "Email Not Verified"));
            }
            // Vérifier si le compte est actif (non bloqué)
            if (!userDetailsService.isEnabled(loginRequest.getEmail())) {
                LOGGER.info("Account is blocked");
                return ResponseEntity.ok(new ApiResponse(false, "Your Account is Blocked please try to Contact Clinical Admin"));
            }

            // Générer le token JWT et le refresh token et effectuer la connexion
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // Générer les tokens d'access et refresh
            JwtTokens tokens = jwtService.generateTokens(loginRequest.getEmail());
            String accessToken = tokens.getAccessToken();
            String refreshToken = tokens.getRefreshToken();

            User user = userServices.findById(userDetails.getId());
            System.out.println(user.getEmail());

            globalVariables.setConnectedUser(user);
            System.out.println("accessToken :"+accessToken+"\n");
            System.out.println("refreshToken :"+refreshToken);
            System.out.println("-----------------------------------------");
            // Mettre à jour la date de dernière connexion et créer une activité de connexion
            autService.updateLastLoginDate(userDetails.getId());
            activityServices.createActivity(new Date(), "Login", "Authentication reussi", user);
            LOGGER.info("Authentication reussi");

            if (userDetails.getRole().equals(ERole.ROLE_MEDECIN)) {
                // Récupérer la demande et le médecin en toute sécurité
                Demande demande = demandeService.findDemandeByConnectedUser(userDetails.getId());
                Medecin medecin = medecinRepository.getMedecinByUserId(userDetails.getId());

                Optional<Subscription> subscription = subscriptionService.getUserLatestSubscription(userDetails.getId());

                // Initialisation du nombre de documents à 0
                int documentsCount = 0;

                // Vérifier si le médecin est non null avant de récupérer ses documents
                if (medecin != null) {
                    List<DocumentsCabinet> documentsCabinets = documentsCabinetServices.getDocumentsByMedecin(medecin.getId());
                    documentsCount = documentsCabinets.size();
                }

                // Vérifier si la demande est non null avant d'accéder à son état
                int demandeState = (demande != null) ? demande.getState() : 0;

                if(subscription.isPresent()) {
                    return ResponseEntity.ok(new JwtResponse(
                            accessToken,
                            userDetails.getId(),
                            userDetails.getEmail(),
                            userDetails.getTelephone(),
                            userDetails.getRole(),
                            refreshToken,
                            demandeState,
                            documentsCount,
                            subscription.get().getPaymentStatus()
                    ));
                }

                return ResponseEntity.ok(new JwtResponse(
                        accessToken,
                        userDetails.getId(),
                        userDetails.getEmail(),
                        userDetails.getTelephone(),
                        userDetails.getRole(),
                        refreshToken,
                        demandeState,
                        documentsCount
                ));
            }

            // Retourner la réponse avec le token JWT et les détails de l'utilisateur
            return ResponseEntity.ok(new JwtResponse(accessToken, userDetails.getId(), userDetails.getEmail(), userDetails.getTelephone(), userDetails.getRole(), refreshToken));
        } catch (UsernameNotFoundException e) {
            // Aucun compte associé à cet email
            System.out.println("no account");
            return ResponseEntity.ok(new ApiResponse(false, "no_account"));
        } catch (BadCredentialsException e) {
            System.out.println("incorrect");
            LOGGER.error("SignIn error" , e);
            // Mot de passe incorrect
            return ResponseEntity.ok(new ApiResponse(false, "incorrect_password"));
        } catch (Exception e) {
            LOGGER.error("An error occurred during authentication: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }


    //INSCRIPTION--------------------------------------------------------------
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {
        try {

            ResponseEntity<?> response = userServices.registerNewUser(signUpRequest);
            return response.getStatusCode().is2xxSuccessful()
                    ? ResponseEntity.ok(new ApiResponse(true, "User registered successfully"))
                    : response;
        } catch (Exception e) {
            LOGGER.error("An error occurred during user registration: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiError(false, "An error occurred while registering new user."));
        }
    }

    // Confirmation du compte(activation du compte)------------------------------------------------
    @GetMapping("/confirmaccount")
    public ResponseEntity<?> confirmAccount(@RequestParam String token) {
        ConfirmationToken confirmationToken = autService.findByConfirmationToken(token);

        if (confirmationToken == null) {
            throw new BadRequestException("Token invalide");
        }

        User user = confirmationToken.getUser();
        Calendar calendar = Calendar.getInstance();


        if ((confirmationToken.getExpiryDate().getTime() - calendar.getTime().getTime()) <= 0) {

            String newLink = baseUrl +"/api/auth/generateNewLink?token=" + token;
            return ResponseEntity.badRequest().body("Lien expiré, générer un nouveau <a href=\"" + newLink + "\">Ici</a>");

        }

        user.setEmailVerified(true);
        user.setEnabled(true);
        autService.save(user);
        LOGGER.info("Compte vérifié avec succès: " + user.getEmail());
        return ResponseEntity.ok("Le compte a été vérifié avec succès!");
    }

    //Generate a new link for the user
    @GetMapping("/generateNewLink")
    public ResponseEntity<String> generateNewLink(@RequestParam String token) {
        ConfirmationToken confirmationToken = autService.findByConfirmationToken(token);

        if (confirmationToken == null) {
            throw new BadRequestException("Invalid token");
        }

        User user = confirmationToken.getUser();

        // Vérifirier si le compte de l'utilisateur est déjà activé
        if (user.getEmailVerified()) {

            return ResponseEntity.badRequest().body("Votre compte est déjà activé.");
        }

        ConfirmationToken newToken = autService.createToken(user);

        String newLink = newToken.getConfirmationToken();


        emailSenderService.sendMailConfirmationNewlink(user.getEmail(), newLink);

        return ResponseEntity.ok("Un nouveau lien vous a été envoyer");
    }


    //Le Cas où un user veut demander un autre si il a egarer le premier
    @GetMapping("/newconfirmationLink")
    public ResponseEntity<?> sendVerificationMail(@Valid @RequestBody VerifyEmailRequest emailRequest) {

        if (autService.existsByEmail(emailRequest.getEmail())) {

            if (userDetailsService.isAccountVerified(emailRequest.getEmail())) {
                throw new BadRequestException("Email est déjà vérifié ");
            } else {
                User user = autService.findByEmail(emailRequest.getEmail());
                ConfirmationToken token = autService.createToken(user);

                emailSenderService.sendMail(user.getEmail(), token.getConfirmationToken());
                return ResponseEntity.ok(new ApiResponse(true, "Un lien de vérification a été envoyé par mail"));
            }
        } else {
            throw new BadRequestException("Email non associé ");
        }
    }

    //Verifification du token-----------------------------------------

   @GetMapping("/checkToken/{token}")
    public Boolean verifierValiditeToken(@PathVariable String token) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(jwtService.extractUsername(token));
            System.out.println("token vérifié: " + token);
            return jwtService.validateToken(token, userDetails);
        } catch (ExpiredJwtException ex) {
            System.out.println("Le jeton est expiré: " + ex.getMessage());
            return false;
        } catch (Exception e) {
            // Autres exceptions non liées à l'expiration du jeton
            System.out.println("Une erreur est survenue lors de la validation du jeton: " + e.getMessage());
            return false;
        }
    }


   /* @GetMapping("/checkToken/{accessToken}")
    public ResponseEntity<?> verifyToken(@PathVariable String accessToken) {
        try {
            // Vérifier si le jeton d'accès est valide
            UserDetails userDetails = userDetailsService.loadUserByUsername(jwtService.extractUsername(accessToken));
            if (userDetails != null && jwtService.validateToken(accessToken, userDetails)) {
                // Le jeton d'accès est valide, retourner une réponse réussie
                return ResponseEntity.ok(new ApiResponse(true, "Token valide"));
            } else {
                // Le jeton d'accès est invalide, retourner une réponse indiquant que le jeton est invalide
                return ResponseEntity.ok(new ApiResponse(false, "Le jeton d'accès est invalide"));
            }
        } catch (Exception e) {
            // Une erreur s'est produite lors de la vérification du jeton
            System.out.println("Erreur lors de la vérification du token : " + e.getMessage());
            // Si c'est une exception d'expiration de token, retournez une réponse indiquant que le token est expiré
            if (e instanceof ExpiredJwtException) {
                return ResponseEntity.ok(new ApiResponse(false, "Le jeton d'accès est expiré"));
            } else {
                // Sinon, retournez une réponse indiquant une erreur générale
                return ResponseEntity.ok(new ApiResponse(false, "Erreur lors de la vérification du token"));
            }
        }
    }*/
    //-----------------------------------------------------------------
    //recuperation d'un token de confirmation par user_id
    @GetMapping("/confirmationtoken/{userId}")
    public ResponseEntity<String> getConfirmationToken(@PathVariable Long userId) {

        ConfirmationToken confirmationToken = autService.getConfirmationTokenByUserId(userId);
        System.out.println("userid" + userId);
        if (confirmationToken == null) {
            return ResponseEntity.notFound().build();
        }

        String tokenValue = confirmationToken.getConfirmationToken();
        return ResponseEntity.ok(tokenValue);
    }


    //RESEND EMAIL---------------------------------

    @PostMapping("/resendConfirmation")
    public ResponseEntity<?> resendConfirmationEmail(@RequestBody ResendEmailRequest resendEmailRequest) {
        try {
            String email = resendEmailRequest.getEmail();
            if (resendEmailRequest.getEmail() == null || resendEmailRequest.getEmail().isEmpty()) {
                throw new BadRequestException("L'adresse e-mail n'est pas fournie");
            }


            if (autService.existsByEmail(resendEmailRequest.getEmail())) {

                if (userDetailsService.isAccountVerified(resendEmailRequest.getEmail())) {
                    throw new BadRequestException("Email est déjà vérifié ");
                } else {
                    User user = autService.findByEmail(resendEmailRequest.getEmail());
                    ConfirmationToken token = autService.createToken(user);

                    emailSenderService.sendMail(user.getEmail(), token.getConfirmationToken());
                    return ResponseEntity.ok(new ApiResponse(true, "Un lien de vérification a été envoyé par mail"));
                }
            } else {
                throw new BadRequestException("Email non associé ");
            }
        } catch (BadRequestException e) {
            // Gestion de l'exception BadRequestException
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            // Gestion des autres exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Une erreur s'est iduite lors de l'envoi du mail de confirmation."));
        }
    }

    //MOT DE PASSE OUBLIÉ-------------------------------------------------------------------
    @PostMapping("/forgotpassword")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgetpwdRequest forgetpwdRequest) {
        try {
            // Vérifier si l'e-mail et la date de naissance sont fournis
            if (forgetpwdRequest.getPatientEmail() != null && !forgetpwdRequest.getPatientEmail().isEmpty() && forgetpwdRequest.getDateNaissance() != null) {
                // Récupérer le patient associé à l'e-mail

                Optional<Patient> patientOptional = autService.findByPatientEmail(forgetpwdRequest.getPatientEmail());

                if (patientOptional.isPresent()) {
                    Patient patient = patientOptional.get();

                    if (patient.getDateNaissance() != null) {
                        //Formattage de la date de naissance du Patient
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                        String formattedPatientDateOfBirth = dateFormat.format(patient.getDateNaissance());//formatter la date de nainssance du patient
                        String formattedUserDateOfBirth = dateFormat.format(forgetpwdRequest.getDateNaissance());//Formatter à nouveau la date de naissance entrer
                        // Vérifier si la date de naissance fournie correspond à celle du patient
                        if (formattedPatientDateOfBirth.equals(formattedUserDateOfBirth)) {


                            // Si les informations fournies par l'utilisateur correspondent, on vas générer le token de réinitialisation
                            PasswordResetToken resetToken = autService.createRestetToken(patient.getUser());
                            String tokenValue = resetToken.getResetToken();

                            // Envoie de l'e-mail de réinitialisation à l'utilisateur
                            emailSenderService.sendResetPasswordMail(forgetpwdRequest.getPatientEmail(), tokenValue);
                            // Retourner un message de succès
                            System.out.println("Un e-mail de réinitialisation a été envoyé à l'adresse " + forgetpwdRequest.getPatientEmail());
                            return ResponseEntity.ok(new ApiResponse(true, "Un e-mail de réinitialisation a été envoyé à l'adresse " + forgetpwdRequest.getPatientEmail()));

                        } else {
                            // Si les informations fournies par l'utilisateur ne correspondent pas, retourner un message d'erreur
                            System.out.println("Les informations fournies ne correspondent pas. Veuillez vérifier votre e-mail et votre date de naissance");
                            return ResponseEntity.ok(new ApiResponse(false, "Veuillez vérifier les informations saisies, puis réessayez !"));

                        }
                    } else {
                        // La date de naissance du patient n'est pas disponible, retourner un message d'erreur
                        return ResponseEntity.ok(new ApiResponse(false, "La date de naissance pour l'utilisateur associé à l'e-mail fourni n'est pas disponible. Veuillez contacter le support."));
                    }


                } else {
                    System.out.println("Aucun patient n'a été retrouvé: " + forgetpwdRequest.getPatientEmail());
                    return ResponseEntity.ok(new ApiResponse(false, "Veuillez vérifier les informations saisies, puis réessayez !"));
                }
            } else {
                System.out.println("L'e-mail et la date de naissance sont requis");
                return ResponseEntity.ok(new ApiResponse(false, "L'e-mail et la date de naissance sont requis."));

            }
        } catch (HttpClientErrorException.Unauthorized e) {
            System.out.println("Erreur de traitement. Veuillez réessayer plus tard");
            return ResponseEntity.ok(new ApiResponse(false, "Erreur de traitement. Veuillez réessayer plus tard."));

        }
    }

    //Checking du jeton de reinitialisation afin de voir la validité
    @GetMapping("/passwordresettoken")
    public ResponseEntity<String> getPasswordResetToken(@RequestParam String resetToken) {
        PasswordResetToken passwordResetToken = autService.findByResetToken(resetToken);
        Calendar calendar = Calendar.getInstance();
        if (passwordResetToken == null) {
            return ResponseEntity.notFound().build();
        }
        if ((passwordResetToken.getExpiryDate().getTime() - calendar.getTime().getTime()) <= 0) {

            LOGGER.info("TOKEN EXPIRÉ");
            return ResponseEntity.badRequest().body("Le lien de réinitialisation du mot de passe a expiré.");
        }
        LOGGER.info("TOKEN VALIDÉ");
        return ResponseEntity.ok(passwordResetToken.getResetToken());
    }

    //RESET PASSWORD-------------------------------
    @PostMapping("/resetpassword")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest, @RequestParam("reset") String resetToken) {
        PasswordResetToken token = autService.findByResetToken(resetToken);

        if (token != null) {
            // Vérifiez si le jeton est encore valide
            if (!token.isExpired()) {
                // Mettez à jour le mot de passe de l'utilisateur avec le nouveau mot de passe fourni
                User user = token.getUser();
                user.setPassword(encoder.encode(resetPasswordRequest.getNewPassword()));
                autService.save(user);
                // Supprimez le jeton de réinitialisation car il a été utilisé
                autService.deleteByResetToken(resetToken);

                return ResponseEntity.ok(new ApiResponse(true, "Mot de passe réinitialisé avec succès."));
            } else {
                // Le jeton de réinitialisation a expiré
                return ResponseEntity.ok(new ApiResponse(false, "Le jeton de réinitialisation a expiré. Veuillez demander un nouveau lien de réinitialisation."));
            }
        } else {
            // Aucun jeton de réinitialisation trouvé pour le token fourni
            return ResponseEntity.badRequest().body(new MessageResponse("Jeton de réinitialisation invalide."));
        }
    }


    //MOT DE PASSE OUBLIÉ POUR ROLE MEDECIN ET SECRETAIRE, PARTIE PRO
    @PostMapping("/forgotpasswordPro")
    public ResponseEntity<?> forgotPasswordPro(@RequestBody ForgetPwdProRequest forgetPwdProRequest) {
        try {
            // Vérifier si l'e-mail et la date de naissance sont fournis
            System.out.println("forgetPwdProRequest: " + forgetPwdProRequest);
            if (forgetPwdProRequest.getProEmail() != null && !forgetPwdProRequest.getProEmail().isEmpty() && forgetPwdProRequest.getDateNaissance() != null) {
                // Récupérer le user associé à l'e-mail

                User user = userServices.findByEmail(forgetPwdProRequest.getProEmail());

                if (user!=null) {
                    Date dateNaissance = null;
                    switch (user.getRole()) {
                        case ROLE_MEDECIN :
                            Medecin medecin= medecinServiceImpl.getMedecinByUserId(user.getId());
                            dateNaissance= medecin.getDateNaissance();
                            break;

                        case ROLE_SECRETAIRE:
                            Secretaire secretaire= secretaireService.findById(user.getId());
                            dateNaissance= secretaire.getDateNaissance();
                            break;

                        //other case here

                        default:
                            break;
                    }

                    if (dateNaissance != null) {
                        //Formattage de la date de naissance du Patient
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                        String formattedProDateOfBirth = dateFormat.format(dateNaissance);//formatter la date de nainssance du patient
                        String formattedUserDateOfBirth = dateFormat.format(forgetPwdProRequest.getDateNaissance());//Formatter à nouveau la date de naissance entrer

                        // Vérifier si la date de naissance fournie correspond à celle du pro user
                        if (formattedProDateOfBirth.equals(formattedUserDateOfBirth)) {


                            // Si les informations fournies par l'utilisateur correspondent, on va générer le token de réinitialisation
                            PasswordResetToken resetToken = autService.createRestetToken(user);
                            String tokenValue = resetToken.getResetToken();

                            // Envoie de l'e-mail de réinitialisation à l'utilisateur
                            emailSenderService.sendResetPasswordMail(forgetPwdProRequest.getProEmail(), tokenValue);
                            // Retourner un message de succès
                            System.out.println("Un e-mail de réinitialisation a été envoyé à l'adresse " + forgetPwdProRequest.getProEmail());
                            return ResponseEntity.ok(new ApiResponse(true, "Un e-mail de réinitialisation a été envoyé à l'adresse " + forgetPwdProRequest.getProEmail()));

                        } else {
                            // Si les informations fournies par l'utilisateur ne correspondent pas, retourner un message d'erreur
                            System.out.println("Les informations fournies ne correspondent pas. Veuillez vérifier votre e-mail et votre date de naissance");
                            return ResponseEntity.ok(new ApiResponse(false, "Les informations fournies ne correspondent pas. Veuillez vérifier votre e-mail et votre date de naissance."));

                        }
                    } else {
                        // La date de naissance du patient n'est pas disponible, retourner un message d'erreur
                        return ResponseEntity.ok(new ApiResponse(false, "La date de naissance pour l'utilisateur associé à l'e-mail fourni n'est pas disponible. Veuillez contacter le support."));
                    }
                } else {
                    System.out.println("Aucun utilisateur n'a été retrouvé avec l'email: " + forgetPwdProRequest.getProEmail());
                    return ResponseEntity.ok(new ApiResponse(false, "Aucun user trouvé pour l'e-mail: " + forgetPwdProRequest.getProEmail()));
                }
            } else {
                System.out.println("L'e-mail et la date de naissance sont requis");
                return ResponseEntity.ok(new ApiResponse(false, "L'e-mail et la date de naissance sont requis."));

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.ok(new ApiResponse(false, "Erreur de traitement. Veuillez réessayer plus tard."));

        }
    }

    @PostMapping("/choose-password")
    public ResponseEntity<?> choosePassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        try {
            // Log password
            // Récupérer l'utilisateur connecté
            User user = userRepository.getById(globalVariables.getConnectedUser().getId());

            // Vérifier si le nouveau mot de passe est valide et différent de l'ancien
            if (resetPasswordRequest.getNewPassword() == null || resetPasswordRequest.getNewPassword().isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiError(false, "Le mot de passe ne peut pas être vide."));
            }
            if (encoder.matches(resetPasswordRequest.getNewPassword(), user.getPassword())) {
                return ResponseEntity.badRequest().body(new ApiError(false, "Le nouveau mot de passe ne peut pas être identique à l'ancien."));
            }
            // Mettre à jour le mot de passe de l'utilisateur
            user.setPassword(encoder.encode(resetPasswordRequest.getNewPassword()));
            userRepository.save(user);
            // Envoyer un e-mail de confirmation de changement de mot de passe
            emailSenderService.sendMailChangePassword(user.getEmail());

            return ResponseEntity.ok(new ApiResponse(true, "Mot de passe mis à jour avec succès."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiError(false, "Une erreur s'est produite lors de la mise à jour du mot de passe."));
        }
    }

	@PostMapping("/checkPassword")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PATIENT' , 'ROLE_MEDECIN')")
	public ResponseEntity<?> checkPassword(@Valid @RequestBody String password)throws Exception {
		User user = userRepository.getById(globalVariables.getConnectedUser().getId());
	    
	    // Vérifier si le mot de passe fourni correspond au mot de passe de l'utilisateur connecté
	    boolean passwordMatch = encoder.matches(password, user.getPassword());
	    
	    if (passwordMatch) {
	        return ResponseEntity.ok(new ApiResponse(true, "Password matches"));
	    } else {
	        return ResponseEntity.ok(new ApiResponse(false, "Password does not match"));
	    }
	}

// ------------------------RENSEND EMAIL FORGOT PASSWORD PATIENT------------

    @PostMapping("/resendforgotresetlink")
    public ResponseEntity<?> resendResetForgotpwsLink(@RequestBody ResendEmailRequest resendEmailRequest) {
        try {
            // Vérifier si l'e-mail est fourni
            if (resendEmailRequest.getEmail() != null && !resendEmailRequest.getEmail().isEmpty()) {
                Optional<Patient> patientOptional = autService.findByPatientEmail(resendEmailRequest.getEmail());

                if (patientOptional.isPresent()) {
                    Patient patient = patientOptional.get();

                    // Générer un nouveau token de réinitialisation
                    PasswordResetToken newResetToken = autService.createRestetToken(patient.getUser());
                    String tokenValue = newResetToken.getResetToken();

                    // Envoyer le nouvel e-mail de réinitialisation
                    emailSenderService.sendResetPasswordMail(resendEmailRequest.getEmail(), tokenValue);

                    return ResponseEntity.ok(new ApiResponse(true, "Un nouvel e-mail de réinitialisation a été envoyé à l'adresse " + resendEmailRequest.getEmail()));
                } else {
                    return ResponseEntity.ok(new ApiResponse(false, "Aucun utilisateur n'a été trouvé avec cet e-mail. Veuillez vérifier les informations saisies, puis réessayez."));
                }
            } else {
                return ResponseEntity.ok(new ApiResponse(false, "L'e-mail est requis pour renvoyer le lien de réinitialisation."));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(false, "Erreur de traitement. Veuillez réessayer plus tard."));
        }
    }

    //Pour le cas du lien expirer
   @PostMapping("/resendresetlink")
   public ResponseEntity<?> resendResetLink(@RequestParam("reset") String resetToken) {
       try {
           PasswordResetToken token = autService.findByResetToken(resetToken);

           if (token != null) {
               // Jeton trouvé, vérifiez s'il est expiré
               if (token.isExpired()) {
                   // Supprimer le jeton expiré
                   autService.deleteByResetToken(resetToken);

                   // Récupérer l'utilisateur associé au jeton
                   User user = token.getUser();
                   String email = user.getEmail();

                   // Générer un nouveau jeton de réinitialisation
                   PasswordResetToken newResetToken = autService.createRestetToken(user);
                   String newTokenValue = newResetToken.getResetToken();

                   // Envoyer le nouvel e-mail de réinitialisation
                   emailSenderService.sendResetPasswordMail(email, newTokenValue);

                   return ResponseEntity.ok(new ApiResponse(true, "Un nouvel e-mail de réinitialisation a été envoyé à l'adresse " + email));
               } else {
                   return ResponseEntity.ok(new ApiResponse(false, "Le jeton est encore valide. Aucune action nécessaire."));
               }
           } else {
               return ResponseEntity.badRequest().body(new ApiResponse(false, "Jeton de réinitialisation invalide."));
           }
       } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Erreur de traitement. Veuillez réessayer plus tard."));
       }
   }


}


