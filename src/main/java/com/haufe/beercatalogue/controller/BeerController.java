package com.haufe.beercatalogue.controller;

import java.util.Optional;

import com.haufe.beercatalogue.dto.BeerDetailDTO;
import com.haufe.beercatalogue.dto.BeerSummaryDTO;
import com.haufe.beercatalogue.dto.PageResponse;
import com.haufe.beercatalogue.dto.BeerCreateDTO;
import com.haufe.beercatalogue.service.BeerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/beers")
public class BeerController {

    private final BeerService service;

    public BeerController(BeerService service) {
        this.service = service;
    }

    @Operation(summary = "Get a paginated list of beers." +
        "This endpoint supports filtering by name, type, abv, and manufacturer ID, as well as sorting by any field.")
    @GetMapping
    public ResponseEntity<PageResponse<BeerSummaryDTO>> getList(
            @Parameter(description = "If present, filter the list by name") @RequestParam(required = false) String name,
            @Parameter(description = "If present, filter the list by type") @RequestParam(required = false) String type,
            @Parameter(description = "If present, filter the list by abv") @RequestParam(required = false) Double abv,
            @Parameter(description = "If present, filter the list by manufacturerId") @RequestParam(required = false) Long manufacturerId,
            @Parameter(description = "Can be 'name', 'type', 'abv' or 'manufacturer'") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Must be 'asc' or 'desc'") @RequestParam(required = false, defaultValue = "asc") String dir,
            @Parameter(description = "Number of the page to fetch") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Size of the page to fetch") @RequestParam(required = false, defaultValue = "50") int size) {

        Page<BeerSummaryDTO> beerPage = service.getList(page, size, sortBy, dir, name, type, abv, manufacturerId);
        return ResponseEntity.ok(new PageResponse<>(beerPage));
    }

    @Operation(summary = "Get a beer by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<BeerDetailDTO> getById(
            @Parameter(description = "ID of the beer to retrieve") @PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Create a new beer. Only accessible to MANUFACTURER or ADMIN roles")
    @PostMapping
    public ResponseEntity<BeerDetailDTO> create(
            @Valid @RequestBody BeerCreateDTO beer) {
        BeerDetailDTO saved = service.create(beer);
        return ResponseEntity.created(null).body(saved);
    }

    @Operation(summary = "Update an existing beer. Only accessible to MANUFACTURER or ADMIN roles")
    @PutMapping("/{id}")
    public ResponseEntity<BeerDetailDTO> update(
            @Parameter(description = "Id of the beer to modify") @PathVariable Long id,
            @Valid @RequestBody BeerCreateDTO beer) {
        Optional<BeerDetailDTO> updated = service.update(id, beer);
        return updated
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Deletes and existing beer. Only accessible to MANUFACTURER or ADMIN roles")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id of the beer to delete") @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}
