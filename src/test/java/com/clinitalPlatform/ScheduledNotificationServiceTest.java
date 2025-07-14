package com.clinitalPlatform;

import com.clinitalPlatform.enums.RdvStatutEnum;
import com.clinitalPlatform.models.*;
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

    @Mock
    private NotificationRepository notificationRepository;

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

        Specialite specialite = new Specialite();
        specialite.setLibelle("Cardiologie");


        medecin = new Medecin();
        medecin.setNom_med("Dupont");
        medecin.setPrenom_med("Jean");
        medecin.setSpecialite(specialite);

        rendezvous = new Rendezvous();
        rendezvous.setId(1L);
        rendezvous.setPatient(patient);
        rendezvous.setMedecin(medecin);
        rendezvous.setStart(LocalDateTime.now().plusHours(12));
    }

    @Test
    void testSendAppointmentReminders() {
        when(rendezvousRepository.findByStartBetween(any(), any())).thenReturn(List.of(rendezvous));

        scheduledNotificationService.sendAppointmentReminders();

        verify(pushNotificationService, times(1)).sendAppointmentReminder(
                eq(1L),
                anyString(),
                anyString(),
                anyString(),
                any(LocalDateTime.class),
                eq(rendezvous.getId()) ,
                rendezvous
        );
    }


    @Test
    void testNotifyCanceledAppointments() {
        rendezvous.setStatut(RdvStatutEnum.ANNULE);
        rendezvous.setCanceledAt(LocalDateTime.now());

        when(rendezvousRepository.findByStatutAndCanceledAtAfter(eq(RdvStatutEnum.ANNULE), any()))
                .thenReturn(List.of(rendezvous));

        scheduledNotificationService.notifyCanceledAppointments();

        verify(pushNotificationService, times(1)).sendAppointmentCancellation(
                eq(1L),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(LocalDateTime.class),
                eq(rendezvous.getId()) ,
                rendezvous
        );
    }

}
