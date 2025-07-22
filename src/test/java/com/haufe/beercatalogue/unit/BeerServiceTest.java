package com.haufe.beercatalogue.unit;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.haufe.beercatalogue.dto.BeerCreateDTO;
import com.haufe.beercatalogue.dto.BeerDetailDTO;
import com.haufe.beercatalogue.dto.BeerSummaryDTO;
import com.haufe.beercatalogue.mapper.BeerMapper;
import com.haufe.beercatalogue.model.Beer;
import com.haufe.beercatalogue.model.Manufacturer;
import com.haufe.beercatalogue.repository.BeerRepository;
import com.haufe.beercatalogue.repository.ManufacturerRepository;
import com.haufe.beercatalogue.service.BeerService;
import com.haufe.beercatalogue.exception.ResourceNotFoundException;
import com.haufe.beercatalogue.exception.BadRequestException;
import com.haufe.beercatalogue.service.BeerAuthorizationService;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class BeerServiceTest {
    
    @Mock
    private BeerAuthorizationService beerAuthorizationService;

    @Mock
    private BeerRepository beerRepository;

    @Mock
    private ManufacturerRepository manufacturerRepository;

    @Mock
    private BeerMapper mapper;

    @InjectMocks
    private BeerService beerService;

    private Beer beer;
    private BeerDetailDTO detailDTO;
    private BeerCreateDTO createDTO;

    @BeforeEach
    void setup() {
        Manufacturer manufacturer = new Manufacturer(1L, "BrewDog", "UK");

        beer = new Beer("Punk IPA", 5.6, "IPA", "Hoppy and bitter", manufacturer);
        beer.setId(1L);

        createDTO = new BeerCreateDTO("Punk IPA", 5.6, "IPA", "Hoppy and bitter", 1L);
        detailDTO = new BeerDetailDTO(1L, "Punk IPA", 5.6, "IPA", "Hoppy and bitter", 1L);
    }

    @Test
    void getById_ReturnsDetailDTO_WhenBeerExists() {
        when(beerRepository.findById(1L)).thenReturn(Optional.of(beer));
        when(mapper.toDetail(beer)).thenReturn(detailDTO);

        BeerDetailDTO result = beerService.getById(1L);

        assertNotNull(result);
        assertEquals("Punk IPA", result.getName());
    }

    @Test
    void getById_ThrowsResourceNotFound_WhenBeerNotFound() {
        when(beerRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> beerService.getById(99L)
        );

        assertEquals("Beer with id 99 not found", exception.getMessage());
    }

    @Test
    void create_ReturnsDetailDTO_WhenBeerIsCreated() {
        Manufacturer manufacturer = beer.getManufacturer();
        when(beerAuthorizationService.canCreateBeer(manufacturer.getId())).thenReturn(true);
        when(manufacturerRepository.findById(1L)).thenReturn(Optional.of(manufacturer));
        when(mapper.toEntity(createDTO, manufacturer)).thenReturn(beer);
        when(beerRepository.save(beer)).thenReturn(beer);
        when(mapper.toDetail(beer)).thenReturn(detailDTO);

        BeerDetailDTO result = beerService.create(createDTO);

        assertEquals("Punk IPA", result.getName());
        verify(beerRepository).save(beer);
    }

    @Test
    void create_ThrowsResourceNotFound_WhenManufacturerNotFound() {
        when(beerAuthorizationService.canCreateBeer(1L)).thenReturn(true);
        when(manufacturerRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> beerService.create(createDTO)
        );

        assertEquals("Manufacturer with id 1 not found", exception.getMessage());
    }

    @Test
    void update_ReturnsDetailDTO_WhenBeerIsUpdated() {
        when(beerAuthorizationService.canEditBeer(1L, createDTO)).thenReturn(true);
        when(beerRepository.findById(1L)).thenReturn(Optional.of(beer));
        when(manufacturerRepository.findById(1L)).thenReturn(Optional.of(beer.getManufacturer()));
        when(beerRepository.save(beer)).thenReturn(beer);
        when(mapper.toDetail(beer)).thenReturn(detailDTO);

        Optional<BeerDetailDTO> result = beerService.update(1L, createDTO);

        assertTrue(result.isPresent());
        assertEquals("Punk IPA", result.get().getName());
    }

    @Test
    void delete_DeletesBeer_WhenExists() {
        when(beerAuthorizationService.canDeleteBeer(1L)).thenReturn(true);
        when(beerRepository.findById(1L)).thenReturn(Optional.of(beer));
        beerService.delete(1L);
        verify(beerRepository).delete(beer);
    }

    @Test
    void findAllSorted_ReturnsBeersSortedList() {
        Beer beerA = new Beer("Alpha", 5.0, "IPA", "Light", beer.getManufacturer());
        Beer beerB = new Beer("Bravo", 6.0, "Stout", "Strong", beer.getManufacturer());
        beerA.setId(1L);
        beerB.setId(2L);

        List<Beer> sortedBeers = List.of(beerA, beerB);
        Pageable pageable = PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "name"));
        Page<Beer> beerPage = new PageImpl<>(sortedBeers, pageable, sortedBeers.size());

        Specification<Beer> spec = null;
        when(beerRepository.findAll(spec, pageable)).thenReturn(beerPage);

        when(mapper.toSummary(beerA)).thenReturn(new BeerSummaryDTO(1L, "Alpha"));
        when(mapper.toSummary(beerB)).thenReturn(new BeerSummaryDTO(2L, "Bravo"));

        Page<BeerSummaryDTO> resultPage = beerService.getList(0, 50, "name", "asc", null, null, null, null);
        List<BeerSummaryDTO> result = resultPage.getContent();

        assertEquals(2, result.size());
        assertEquals("Alpha", result.get(0).getName());
        assertEquals("Bravo", result.get(1).getName());
    }

    @Test
    void findAllSorted_ReturnsBeersSortedByAbvDesc() {
        Beer beerA = new Beer("Alpha", 5.0, "IPA", "Light", beer.getManufacturer());
        Beer beerB = new Beer("Bravo", 6.0, "Stout", "Strong", beer.getManufacturer());
        beerA.setId(1L);
        beerB.setId(2L);

        List<Beer> sortedBeers = List.of(beerB, beerA);
        Pageable pageable = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "abv"));
        Page<Beer> beerPage = new PageImpl<>(sortedBeers, pageable, sortedBeers.size());

        Specification<Beer> spec = null;
        when(beerRepository.findAll(spec, pageable)).thenReturn(beerPage);

        when(mapper.toSummary(beerA)).thenReturn(new BeerSummaryDTO(1L, "Alpha"));
        when(mapper.toSummary(beerB)).thenReturn(new BeerSummaryDTO(2L, "Bravo"));

        Page<BeerSummaryDTO> resultPage = beerService.getList(0, 50, "abv", "desc", null, null, null, null);
        List<BeerSummaryDTO> result = resultPage.getContent();

        assertEquals(2, result.size());
        assertEquals("Bravo", result.get(0).getName());
        assertEquals("Alpha", result.get(1).getName());
    }

    @Test
    void findAllSorted_ThrowsBadRequest_ForInvalidSortField() {
        String invalidSort = "unknown";

        BadRequestException ex = assertThrows(
            BadRequestException.class,
            () -> beerService.getList(0, 50, invalidSort, "asc", null, null, null, null)
        );

        assertEquals("Invalid sort field: unknown", ex.getMessage());
    }

    @Test
    void findAllSorted_ThrowsBadRequest_ForInvalidSortDirection() { 
        String invalidDirection = "upward";

        BadRequestException ex = assertThrows(
            BadRequestException.class,
            () -> beerService.getList(0, 50, "name", invalidDirection, null, null, null, null)
        );

        assertEquals("Invalid sort direction: upward", ex.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getList_WithFilters_ReturnsFilteredAndSortedBeers() {
        Beer beerA = new Beer("Alpha", 5.0, "IPA", "Light", beer.getManufacturer());
        Beer beerB = new Beer("Bravo", 6.0, "Stout", "Strong", beer.getManufacturer());
        beerA.setId(1L);
        beerB.setId(2L);

        List<Beer> beers = List.of(beerA, beerB);
        Pageable pageable = PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "name"));
        Page<Beer> beerPage = new PageImpl<>(beers, pageable, beers.size());

        when(beerRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(beerPage);

        when(mapper.toSummary(beerA)).thenReturn(new BeerSummaryDTO(1L, "Alpha"));
        when(mapper.toSummary(beerB)).thenReturn(new BeerSummaryDTO(2L, "Bravo"));

        Page<BeerSummaryDTO> result = beerService.getList(0, 50, "name", "asc", "alp", null, null, null);

        assertEquals(2, result.getContent().size());
        assertEquals("Alpha", result.getContent().get(0).getName());
        assertEquals("Bravo", result.getContent().get(1).getName());

        ArgumentCaptor<Specification<Beer>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(beerRepository).findAll(specCaptor.capture(), eq(pageable));
        Specification<Beer> capturedSpec = specCaptor.getValue();
        assertNotNull(capturedSpec);
    }

}
