package com.clinitalPlatform;

import com.clinitalPlatform.models.MotifFermeture;
import com.clinitalPlatform.repository.MotifFermetureRepository;
import com.clinitalPlatform.services.MotifFermetureService;
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

class MotifFermetureServiceTest {

    @InjectMocks
    private MotifFermetureService motifFermetureService;

    @Mock
    private MotifFermetureRepository motifRepo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        List<MotifFermeture> motifs = Arrays.asList(new MotifFermeture(), new MotifFermeture());
        when(motifRepo.findAll()).thenReturn(motifs);

        List<MotifFermeture> result = motifFermetureService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(motifRepo, times(1)).findAll();
    }

    @Test
    void testFindById() {
        MotifFermeture motif = new MotifFermeture();
        when(motifRepo.findById(1L)).thenReturn(Optional.of(motif));

        MotifFermeture result = motifFermetureService.findById(1L);

        assertNotNull(result);
        verify(motifRepo, times(1)).findById(1L);
    }

    @Test
    void testDelete() {
        MotifFermeture motif = new MotifFermeture();

        motifFermetureService.delete(motif);

        verify(motifRepo, times(1)).delete(motif);
    }
}