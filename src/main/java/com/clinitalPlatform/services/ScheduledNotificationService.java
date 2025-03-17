package com.clinitalPlatform.services;

import com.clinitalPlatform.enums.NotificationType;
import com.clinitalPlatform.enums.RdvStatutEnum;
import com.clinitalPlatform.models.Rendezvous;
import com.clinitalPlatform.models.Patient;
import com.clinitalPlatform.repository.NotificationRepository;
import com.clinitalPlatform.repository.RdvRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScheduledNotificationService {

    @Autowired
    private RdvRepository rendezvousRepository;

    @Autowired
    private NotificationRepository notificationRepository;


    @Autowired
    private PushNotificationService pushNotificationService;

    /**
     * Vérifie chaque heure s'il y a des rendez-vous prévus dans moins de 24h
     * et envoie des notifications aux patients concernés.
     */
    @Scheduled(cron = "0 */20 * * * *")
    public void sendAppointmentReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusHours(24);

        List<Rendezvous> upcomingAppointments = rendezvousRepository.findByStartBetween(now, threshold);

        for (Rendezvous rdv : upcomingAppointments) {
            if (rdv.getPatient() != null) {
                // Vérifier si une notification a déjà été envoyée pour ce rendez-vous
                boolean notificationExists = notificationRepository.existsByRdvIdAndType(rdv.getId(), NotificationType.REMINDER);

                Patient patient = rdv.getPatient();
                String appointmentDetails = "Votre rendez-vous est prévu le "
                        + rdv.getStart().toLocalDate();
                if(!notificationExists){
                    pushNotificationService.sendAppointmentReminder(patient.getUser().getId(), rdv.getMedecin().getSpecialite().getLibelle() , appointmentDetails ,
                            "Dr" + " " + rdv.getMedecin().getNom_med() + " " + rdv.getMedecin().getPrenom_med() , rdv.getStart() , rdv.getId());
                }

            }
        }
    }

    @Scheduled(cron = "0 */30 * * * *") // Exécute toutes les 30 minutes
    public void notifyCanceledAppointments() {
        LocalDateTime lastCheck = LocalDateTime.now().minusMinutes(30);
        List<Rendezvous> canceledAppointments = rendezvousRepository.findByStatutAndCanceledAtAfter(
                RdvStatutEnum.ANNULE, lastCheck
        );

        for (Rendezvous rdv : canceledAppointments) {
            if (rdv.getPatient() != null) {
                boolean notificationExists = notificationRepository.existsByRdvIdAndType(rdv.getId(), NotificationType.ERROR);

                if(!notificationExists) {
                    pushNotificationService.sendAppointmentCancellation(
                            rdv.getPatient().getUser().getId(), rdv.getMedecin().getSpecialite().getLibelle(),
                            "Votre rendez-vous du " + rdv.getStart().toLocalDate() + " a été annulé.",
                            "Dr" + " " + rdv.getMedecin().getNom_med() + " " + rdv.getMedecin().getPrenom_med(),
                            rdv.getStart(),
                            rdv.getId()
                    );
                }
            }
        }
    }

    @Scheduled(cron = "0 0 3 * * ?") // Exécute chaque jour à 03:00 AM
    public void deleteOldNotifications() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        notificationRepository.deleteOldNotifications(threshold);
    }

}
