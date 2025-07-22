package com.haufe.beercatalogue.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import com.haufe.beercatalogue.dto.ManufacturerCreateDTO;
import com.haufe.beercatalogue.dto.ManufacturerDetailDTO;
import com.haufe.beercatalogue.service.ManufacturerService;
import com.haufe.beercatalogue.dto.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/manufacturers")
public class ManufacturerController {

    private final ManufacturerService service;

    public ManufacturerController(ManufacturerService service) {
        this.service = service;
    }

    @Operation(summary = "Get a paginated list of manufacturers.")
    @GetMapping
    public ResponseEntity<PageResponse<ManufacturerDetailDTO>> getList(
            @Parameter(description = "Number of the page to fetch") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Size of the page to fetch") @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(new PageResponse<>(service.getList(pageable)));
    }

    @Operation(summary = "Get a manufacturer by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<ManufacturerDetailDTO> getById(
            @Parameter(description = "ID of the manufacturer to retrieve") @PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Create a new manufacturer. Only accessible to ADMIN role and Manufacturer owner")
    @PostMapping
    public ResponseEntity<ManufacturerDetailDTO> create(
            @Valid @RequestBody ManufacturerCreateDTO manufacturer) {
        ManufacturerDetailDTO saved = service.create(manufacturer);
        return ResponseEntity.created(null).body(saved);
    }

    @Operation(summary = "Update an existing manufacturer. Only accessible to ADMIN role and Manufacturer owner")
    @PutMapping("/{id}")
    public ResponseEntity<ManufacturerDetailDTO> update(
            @Parameter(description = "Id of the manufacturer to update") @PathVariable Long id,
            @Valid @RequestBody ManufacturerCreateDTO manufacturer) {
        Optional<ManufacturerDetailDTO> updated = service.update(id, manufacturer);
        return updated
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Deletes an existing manufacturer. Only accessible to ADMIN role")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id of the manufacturer to delete") @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}