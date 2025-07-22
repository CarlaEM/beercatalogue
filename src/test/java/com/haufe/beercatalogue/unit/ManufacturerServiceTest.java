package com.haufe.beercatalogue.unit;

import com.haufe.beercatalogue.dto.ManufacturerDetailDTO;
import com.haufe.beercatalogue.exception.ResourceNotFoundException;
import com.haufe.beercatalogue.mapper.ManufacturerMapper;
import com.haufe.beercatalogue.model.Manufacturer;
import com.haufe.beercatalogue.repository.ManufacturerRepository;
import com.haufe.beercatalogue.service.ManufacturerAuthorizationService;
import com.haufe.beercatalogue.service.ManufacturerService;
import com.haufe.beercatalogue.dto.ManufacturerCreateDTO;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ManufacturerServiceTest {

    @Mock
    private ManufacturerAuthorizationService manufacturerAuthorizationService;

    @Mock
    private ManufacturerRepository manufacturerRepository;

    @Mock
    private ManufacturerMapper mapper;

    @InjectMocks
    private ManufacturerService manufacturerService;

    private Manufacturer manufacturer;
    private ManufacturerDetailDTO detailDTO;
    private ManufacturerCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        manufacturerAuthorizationService = mock(ManufacturerAuthorizationService.class);
        manufacturerRepository = mock(ManufacturerRepository.class);
        mapper = mock(ManufacturerMapper.class);
        manufacturerService = new ManufacturerService(manufacturerRepository, mapper, manufacturerAuthorizationService);

        manufacturer = new Manufacturer();
        manufacturer.setId(1L);
        manufacturer.setName("Brew Co");
        manufacturer.setCountry("Germany");

        detailDTO = new ManufacturerDetailDTO(1L, "Brew Co", "Germany");
        createDTO = new ManufacturerCreateDTO("Brew Co", "Germany");
    }

    @Test
    void getById_ReturnsManufacturerDetailDTO_WhenFound() {
        when(manufacturerRepository.findById(1L)).thenReturn(Optional.of(manufacturer));
        when(mapper.toDetail(manufacturer)).thenReturn(detailDTO);

        ManufacturerDetailDTO result = manufacturerService.getById(1L);

        assertNotNull(result);
        assertEquals("Brew Co", result.getName());
    }

    @Test
    void getById_Throws_WhenNotFound() {
        when(manufacturerRepository.findById(42L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            manufacturerService.getById(42L);
        });

        assertEquals("Manufacturer with id 42 not found", ex.getMessage());
    }

    @Test
    void findAll_ReturnsDetailsDTOList() {
        Page<Manufacturer> manufacturerPage = new PageImpl<>(List.of(manufacturer), PageRequest.of(0, 10), 1);
        when(manufacturerRepository.findAll(PageRequest.of(0, 50))).thenReturn(manufacturerPage);
        when(mapper.toDetail(manufacturer)).thenReturn(detailDTO);

        List<ManufacturerDetailDTO> result = manufacturerService.getList(PageRequest.of(0, 50)).getContent();

        assertEquals(1, result.size());
        assertEquals("Brew Co", result.get(0).getName());
    }

    @Test
    void delete_DeletesManufacturer_WhenExists() {
        when(manufacturerRepository.findById(1L)).thenReturn(Optional.of(manufacturer));
        when(manufacturerAuthorizationService.canEditManufacturer(1L)).thenReturn(true);
        manufacturerService.delete(1L);
        verify(manufacturerRepository).delete(manufacturer);
    }

    @Test
    void create_ReturnsDetailDTO_WhenManufacturerIsCreated() {
        when(mapper.toEntity(createDTO)).thenReturn(manufacturer);
        when(manufacturerAuthorizationService.canEditManufacturer(1L)).thenReturn(true);
        when(manufacturerRepository.save(manufacturer)).thenReturn(manufacturer);
        when(mapper.toDetail(manufacturer)).thenReturn(detailDTO);

        ManufacturerDetailDTO result = manufacturerService.create(createDTO);

        assertEquals("Brew Co", result.getName());
        verify(manufacturerRepository).save(manufacturer);
    }

}
