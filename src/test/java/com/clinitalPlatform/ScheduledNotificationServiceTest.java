package com.clinitalPlatform;

import com.clinitalPlatform.enums.RdvStatutEnum;
import com.clinitalPlatform.models.Medecin;
import com.clinitalPlatform.models.Patient;
import com.clinitalPlatform.models.Rendezvous;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.repository.NotificationRepository;
import com.clinitalPlatform.repository.RdvRepository;
import com.clinitalPlatform.services.PushNotificationService;
import com.clinitalPlatform.services.ScheduledNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledNotificationServiceTest {

    @Mock
    private RdvRepository rendezvousRepository;

    @Mock
    private PushNotificationService pushNotificationService;

    @InjectMocks
    private ScheduledNotificationService scheduledNotificationService;

    private Patient patient;
    private Medecin medecin;
    private Rendezvous rendezvous;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);

        patient = new Patient();
        patient.setUser(user);
        patient.setId(1L);

        medecin = new Medecin();
        medecin.setNom_med("Dupont");
        medecin.setPrenom_med("Jean");

        rendezvous = new Rendezvous();
        rendezvous.setPatient(patient);
        rendezvous.setMedecin(medecin);
        rendezvous.setStart(LocalDateTime.now().plusHours(12));
    }

    @Test
    void testSendAppointmentReminders() {
        // Simuler un RDV dans moins de 24h
        when(rendezvousRepository.findByStartBetween(any(), any())).thenReturn(List.of(rendezvous));

        // Exécuter la méthode
        scheduledNotificationService.sendAppointmentReminders();

        // Vérifier que la notification est bien envoyée
        verify(pushNotificationService, times(1)).sendAppointmentReminder(
                eq(1L), anyString() , anyString(), anyString() , LocalDateTime.now() , 1L
        );
    }

    @Test
    void testNotifyCanceledAppointments() {
        rendezvous.setStatut(RdvStatutEnum.ANNULE);
        rendezvous.setCanceledAt(LocalDateTime.now());

        when(rendezvousRepository.findByStatutAndCanceledAtAfter(eq(RdvStatutEnum.ANNULE), any()))
                .thenReturn(Arrays.asList(rendezvous));

        scheduledNotificationService.notifyCanceledAppointments();

        verify(pushNotificationService, times(1)).sendAppointmentCancellation(
                eq(1L), anyString() , anyString() , anyString() , LocalDateTime.now() , 1L
        );
    }
}
