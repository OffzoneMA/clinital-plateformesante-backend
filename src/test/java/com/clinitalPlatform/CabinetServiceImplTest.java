package com.clinitalPlatform;

import com.clinitalPlatform.dto.CabinetDTO;
import com.clinitalPlatform.models.*;
import com.clinitalPlatform.payload.request.CabinetRequest;
import com.clinitalPlatform.repository.*;
import com.clinitalPlatform.services.CabinetServiceImpl;
import com.clinitalPlatform.util.ClinitalModelMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CabinetServiceImplTest {

    @InjectMocks
    private CabinetServiceImpl cabinetService;

    @Mock
    private CabinetRepository cabinetRepository;

    @Mock
    private VilleRepository villeRepository;

    @Mock
    private MedecinRepository medecinRepository;

    @Mock
    private PaymentInfoRepository paymentInfoRepository;

    @Mock
    private ClinitalModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByName() throws Exception {
        String name = "TestCabinet";
        Cabinet cabinet = new Cabinet();
        cabinet.setNom(name);

        when(cabinetRepository.findByNomContainingIgnoreCase(name)).thenReturn(List.of(cabinet));
        when(modelMapper.map(cabinet, Cabinet.class)).thenReturn(cabinet);

        List<Cabinet> result = cabinetService.findByName(name);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(name, result.get(0).getNom());
        verify(cabinetRepository, times(1)).findByNomContainingIgnoreCase(name);
    }

    @Test
    void testFindById() throws Exception {
        Long id = 1L;
        Cabinet cabinet = new Cabinet();
        cabinet.setId_cabinet(id);

        when(cabinetRepository.findById(id)).thenReturn(Optional.of(cabinet));

        Optional<Cabinet> result = cabinetService.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId_cabinet());
        verify(cabinetRepository, times(1)).findById(id);
    }

    @Test
    void testCreate() throws Exception {
        CabinetRequest cabinetRequest = new CabinetRequest();
        cabinetRequest.setNom("TestCabinet");
        cabinetRequest.setId_ville(1L);
        cabinetRequest.setIntituleCompte("CompteTest");
        cabinetRequest.setRib("123456");
        cabinetRequest.setCodeSwift("SWIFT123");

        Ville ville = new Ville();
        ville.setId_ville(1L);

        Medecin medecin = new Medecin();
        medecin.setId(1L);

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setIntituleCompte("CompteTest");

        when(villeRepository.findById(1L)).thenReturn(Optional.of(ville));
        when(paymentInfoRepository.save(any(PaymentInfo.class))).thenReturn(paymentInfo);

        Cabinet result = cabinetService.create(cabinetRequest, medecin);

        assertNotNull(result);
        assertEquals("TestCabinet", result.getNom());
        assertEquals(ville, result.getVille());
        assertEquals(paymentInfo, result.getPaymentInfo());
        verify(cabinetRepository, times(1)).save(any(Cabinet.class));
    }

    @Test
    void testAllCabinetsByMedID() throws Exception {
        Long medId = 1L;
        Medecin medecin = new Medecin();
        medecin.setId(medId);

        Cabinet cabinet = new Cabinet();
        cabinet.setNom("TestCabinet");

        when(medecinRepository.findById(medId)).thenReturn(Optional.of(medecin));
        when(cabinetRepository.getAllCabinetByIdMed(medId)).thenReturn(List.of(cabinet));
        when(modelMapper.map(cabinet, Cabinet.class)).thenReturn(cabinet);

        List<Cabinet> result = cabinetService.allCabinetsByMedID(medId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TestCabinet", result.get(0).getNom());
        verify(cabinetRepository, times(1)).getAllCabinetByIdMed(medId);
    }
}