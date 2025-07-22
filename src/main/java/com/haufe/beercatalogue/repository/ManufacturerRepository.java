package com.haufe.beercatalogue.repository;

import com.haufe.beercatalogue.model.Manufacturer;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ManufacturerRepository extends JpaRepository<Manufacturer, Long> {
    
    Page<Manufacturer> findAll(Pageable pageable);

}