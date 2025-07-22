package com.haufe.beercatalogue.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import com.haufe.beercatalogue.dto.ManufacturerCreateDTO;
import com.haufe.beercatalogue.dto.ManufacturerDetailDTO;
import com.haufe.beercatalogue.exception.ResourceNotFoundException;
import com.haufe.beercatalogue.model.Manufacturer;
import com.haufe.beercatalogue.mapper.ManufacturerMapper;
import com.haufe.beercatalogue.repository.ManufacturerRepository;

import java.util.Optional;

@Service
public class ManufacturerService {

    private final ManufacturerRepository manufacturerRepository;
    private final ManufacturerMapper mapper;
    private final ManufacturerAuthorizationService manufacturerAuthorizationService;

    public ManufacturerService(ManufacturerRepository manufacturerRepository, ManufacturerMapper mapper, ManufacturerAuthorizationService manufacturerAuthorizationService) {
        this.manufacturerRepository = manufacturerRepository;
        this.mapper = mapper;
        this.manufacturerAuthorizationService = manufacturerAuthorizationService;
    }

    public Page<ManufacturerDetailDTO> getList(Pageable pageable) {
        return manufacturerRepository.findAll(pageable).map(mapper::toDetail);
    }

    public ManufacturerDetailDTO getById(Long id) {
        return manufacturerRepository.findById(id)
            .map(mapper::toDetail)
            .orElseThrow(() -> new ResourceNotFoundException("Manufacturer with id " + id + " not found"));
    }
    
    @Transactional
    public ManufacturerDetailDTO create(ManufacturerCreateDTO dto) {
        Manufacturer manufacturer = mapper.toEntity(dto);
        return mapper.toDetail(manufacturerRepository.save(manufacturer));
    }

    @Transactional
    public Optional<ManufacturerDetailDTO> update(Long id, ManufacturerCreateDTO manufacturer) {
        if (!manufacturerAuthorizationService.canEditManufacturer(id)) {
            throw new AccessDeniedException("You do not have permission to modify this manufacturer");
        }

        Manufacturer existing = manufacturerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Manufacturer with id " + id + " not found"));

        existing.setName(manufacturer.getName());
        existing.setCountry(manufacturer.getCountry()); 

        return Optional.of(manufacturerRepository.save(existing)).map(mapper::toDetail);
    }

    @Transactional
    public void delete(Long id) {
        if (!manufacturerAuthorizationService.canEditManufacturer(id)) {
            throw new AccessDeniedException("You do not have permission to delete this manufacturer");
        }

        Manufacturer manufacturer = manufacturerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Manufacturer with id " + id + " not found"));

        manufacturerRepository.delete(manufacturer);
    }

}
