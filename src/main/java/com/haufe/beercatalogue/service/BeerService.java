package com.haufe.beercatalogue.service;

import com.haufe.beercatalogue.dto.BeerCreateDTO;
import com.haufe.beercatalogue.dto.BeerDetailDTO;
import com.haufe.beercatalogue.dto.BeerSummaryDTO;
import com.haufe.beercatalogue.exception.ResourceNotFoundException;
import com.haufe.beercatalogue.mapper.BeerMapper;
import com.haufe.beercatalogue.model.Beer;
import com.haufe.beercatalogue.model.Manufacturer;
import com.haufe.beercatalogue.repository.BeerRepository;
import com.haufe.beercatalogue.repository.ManufacturerRepository;
import com.haufe.beercatalogue.exception.BadRequestException;
import com.haufe.beercatalogue.repository.specification.BeerSpecifications;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.Map;

@Service
public class BeerService {

    private static final Map<String, String> SORT_FIELDS = Map.of("name", "name", "type", "type", "abv", "abv", "manufacturer", "manufacturer_id");

    private final BeerRepository beerRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final BeerAuthorizationService beerAuthorizationService;
    private final BeerMapper mapper;

    public BeerService(BeerRepository beerRepository, ManufacturerRepository manufacturerRepository, BeerMapper mapper, BeerAuthorizationService beerAuthorizationService) {
        this.beerRepository = beerRepository;
        this.manufacturerRepository = manufacturerRepository;
        this.beerAuthorizationService = beerAuthorizationService;
        this.mapper = mapper;
    }
    
    public Page<BeerSummaryDTO> getList(
            int page, int size, String sortBy, String direction,
            String name, String type, Double abv, Long manufacturerId) {
        Sort sort = Sort.unsorted();

        // Validate and set sort direction
        if (sortBy != null && !sortBy.isBlank()) {
            if (!SORT_FIELDS.containsKey(sortBy))
                throw new BadRequestException("Invalid sort field: " + sortBy);

            Sort.Direction sortDirection = Sort.Direction.ASC;
            if (direction != null && !direction.isBlank()) {
                try {
                    sortDirection = Sort.Direction.fromString(direction);
                } catch (IllegalArgumentException ex) {
                    throw new BadRequestException("Invalid sort direction: " + direction);
                }
            }

            sort = Sort.by(sortDirection, SORT_FIELDS.get(sortBy));
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        // Build dynamic specifications based on provided filters
        Specification<Beer> spec = null;
        if (name != null) {
            Specification<Beer> nameSpec = BeerSpecifications.hasNameLike(name);
            spec = (spec == null) ? nameSpec : spec.and(nameSpec);
        }

        if (type != null) {
            Specification<Beer> typeSpec = BeerSpecifications.hasTypeLike(type);
            spec = (spec == null) ? typeSpec : spec.and(typeSpec);
        }

        if (manufacturerId != null) {
            Specification<Beer> manufacturerSpec = BeerSpecifications.hasManufacturerId(manufacturerId);
            spec = (spec == null) ? manufacturerSpec : spec.and(manufacturerSpec);
        }

        if (abv != null) {
            Specification<Beer> abvSpec = BeerSpecifications.hasAbv(abv);
            spec = (spec == null) ? abvSpec : spec.and(abvSpec);
        }

        // Fetch paginated results with applied specifications
        Page<Beer> pageResult = beerRepository.findAll(spec, pageable);
        return pageResult.map(mapper::toSummary);
    }

    public BeerDetailDTO getById(Long id) {
        return beerRepository.findById(id)
                .map(mapper::toDetail)
                .orElseThrow(() -> new ResourceNotFoundException("Beer with id " + id + " not found"));
    }

    @Transactional
    public BeerDetailDTO create(BeerCreateDTO dto) {
        if (!beerAuthorizationService.canCreateBeer(dto.getManufacturerId()))
            throw new AccessDeniedException("You do not have permission to create a beer for this manufacturer");

        Manufacturer manufacturer = manufacturerRepository.findById(dto.getManufacturerId())
            .orElseThrow(() -> new ResourceNotFoundException("Manufacturer with id " + dto.getManufacturerId() + " not found"));

        Beer beer = mapper.toEntity(dto, manufacturer);
        return mapper.toDetail(beerRepository.save(beer));
    }

    @Transactional
    public Optional<BeerDetailDTO> update(Long id, BeerCreateDTO dto) {
        if (!beerAuthorizationService.canEditBeer(id, dto))
            throw new AccessDeniedException("You do not have permission to modify this beer");

        Beer existing = beerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Beer with id " + id + " not found"));

        Manufacturer manufacturer = manufacturerRepository.findById(dto.getManufacturerId())
            .orElseThrow(() -> new ResourceNotFoundException("Manufacturer with id " + dto.getManufacturerId() + " not found"));

        existing.setName(dto.getName());
        existing.setAbv(dto.getAbv());
        existing.setType(dto.getType());
        existing.setDescription(dto.getDescription());
        existing.setManufacturer(manufacturer);

        return Optional.of(beerRepository.save(existing)).map(mapper::toDetail);
    }

    @Transactional
    public void delete(Long id) {
        if (!beerAuthorizationService.canDeleteBeer(id)) {
            throw new AccessDeniedException("You do not have permission to delete this beer");
        }

        Beer beer = beerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Beer with id " + id + " not found"));

        beerRepository.delete(beer);
    }

}