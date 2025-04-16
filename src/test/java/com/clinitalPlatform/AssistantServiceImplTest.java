package com.clinitalPlatform;

import com.clinitalPlatform.models.Assistant;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.repository.AssistantRepository;
import com.clinitalPlatform.services.ActivityServices;
import com.clinitalPlatform.services.AssistantServiceImpl;
import com.clinitalPlatform.util.ClinitalModelMapper;
import com.clinitalPlatform.util.GlobalVariables;
import javassist.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssistantServiceImplTest {

    @InjectMocks
    private AssistantServiceImpl assistantService;

    @Mock
    private AssistantRepository assistantRepository;

    @Mock
    private ClinitalModelMapper clinitalModelMapper;

    @Mock
    private GlobalVariables globalVariables;

    @Mock
    private ActivityServices activityServices;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindById() throws Exception {
        Long id = 1L;

        // Simuler un utilisateur connecté
        User connectedUser = new User();
        connectedUser.setId(1L);
        when(globalVariables.getConnectedUser()).thenReturn(connectedUser);

        // Préparer les données simulées
        Assistant assistant = new Assistant();
        assistant.setId(id);
        when(assistantRepository.findById(id)).thenReturn(Optional.of(assistant));

        // Appeler la méthode testée
        Assistant result = assistantService.findById(id);

        // Vérifications
        assertNotNull(result, "L'assistant ne doit pas être null");
        assertEquals(id, result.getId(), "L'ID de l'assistant doit correspondre");

        // Vérifier les interactions
        verify(globalVariables, times(1)).getConnectedUser();
        verify(assistantRepository, times(1)).findById(id);
        verify(activityServices, times(1)).createActivity(any(Date.class), eq("Read"), eq("Consult Assistant ID:" + id), eq(connectedUser));
    }

    @Test
    void testFindByIdThrowsExceptionWhenUserNotConnected() throws NotFoundException {
        Long id = 1L;

        // Simuler l'absence d'utilisateur connecté
        when(globalVariables.getConnectedUser()).thenReturn(null);

        // Vérifier que l'exception est levée
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            assistantService.findById(id);
        });

        // Vérifier le message de l'exception
        assertEquals("Aucun utilisateur connecté.", exception.getMessage());

        // Vérifier les interactions
        verify(globalVariables, times(1)).getConnectedUser();
        verifyNoInteractions(assistantRepository);
        verifyNoInteractions(activityServices);
    }

    @Test
    void testFindByIdCabinet() {
        Long cabinetId = 1L;
        List<Assistant> assistants = new ArrayList<>();
        Assistant assistant = new Assistant();
        assistant.setId(1L);
        assistants.add(assistant);

        when(assistantRepository.findAssistantsByCabinetId(cabinetId)).thenReturn(assistants);
        when(clinitalModelMapper.map(any(Assistant.class), eq(Assistant.class))).thenReturn(assistant);

        List<Assistant> result = assistantService.findByIdCabinet(cabinetId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(assistantRepository, times(1)).findAssistantsByCabinetId(cabinetId);
    }

    @Test
    void testFindAll() throws NotFoundException {
        // Simuler un utilisateur connecté
        User connectedUser = new User();
        connectedUser.setId(1L);
        when(globalVariables.getConnectedUser()).thenReturn(connectedUser);

        // Préparer les données simulées
        List<Assistant> assistants = new ArrayList<>();
        Assistant assistant = new Assistant();
        assistant.setId(1L);
        assistants.add(assistant);

        // Configurer les mocks
        when(assistantRepository.findAll()).thenReturn(assistants);
        when(clinitalModelMapper.map(any(Assistant.class), eq(Assistant.class))).thenReturn(assistant);

        // Appeler la méthode testée
        List<Assistant> result = assistantService.findAll();

        // Vérifications
        assertNotNull(result, "La liste des assistants ne doit pas être null");
        assertEquals(1, result.size(), "La taille de la liste des assistants doit être 1");
        assertEquals(1L, result.get(0).getId(), "L'ID de l'assistant doit être 1");

        // Vérifier les interactions
        verify(globalVariables, times(1)).getConnectedUser();
        verify(assistantRepository, times(1)).findAll();
        verify(activityServices, times(1)).createActivity(any(Date.class), eq("Read"), eq("Consult All Assistants "), eq(connectedUser));
    }
}
