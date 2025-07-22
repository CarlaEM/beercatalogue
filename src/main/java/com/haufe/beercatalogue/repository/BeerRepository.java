package com.haufe.beercatalogue.repository;

import com.haufe.beercatalogue.model.Beer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BeerRepository extends JpaRepository<Beer, Long>, JpaSpecificationExecutor<Beer> {
    
}