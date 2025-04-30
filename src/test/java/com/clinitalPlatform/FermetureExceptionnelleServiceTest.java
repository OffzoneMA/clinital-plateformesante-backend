package com.clinitalPlatform;

import com.clinitalPlatform.dto.FermetureDTO;
import com.clinitalPlatform.models.FermetureExceptionnelle;
import com.clinitalPlatform.models.MotifFermeture;
import com.clinitalPlatform.repository.FermetureExceptionnelleRepository;
import com.clinitalPlatform.repository.MotifFermetureRepository;
import com.clinitalPlatform.services.FermetureExceptionnelleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FermetureExceptionnelleServiceTest {

    @InjectMocks
    private FermetureExceptionnelleService fermetureService;

    @Mock
    private FermetureExceptionnelleRepository fermetureRepo;

    @Mock
    private MotifFermetureRepository motifRepo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetFermeturesParMedecin() {
        List<FermetureExceptionnelle> fermetures = Arrays.asList(new FermetureExceptionnelle(), new FermetureExceptionnelle());
        when(fermetureRepo.findAllByMedecinId(1L)).thenReturn(fermetures);

        List<FermetureExceptionnelle> result = fermetureService.getFermeturesParMedecin(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(fermetureRepo, times(1)).findAllByMedecinId(1L);
    }

    @Test
    void testSupprimerFermeture() {
        fermetureService.supprimerFermeture(1L);

        verify(fermetureRepo, times(1)).deleteById(1L);
    }
}
