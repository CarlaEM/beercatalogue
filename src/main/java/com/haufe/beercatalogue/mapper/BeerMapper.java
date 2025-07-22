package com.haufe.beercatalogue.mapper;

import com.haufe.beercatalogue.dto.BeerDetailDTO;
import com.haufe.beercatalogue.dto.BeerSummaryDTO;
import com.haufe.beercatalogue.dto.BeerCreateDTO;
import com.haufe.beercatalogue.model.Beer;
import com.haufe.beercatalogue.model.Manufacturer;

import org.springframework.stereotype.Component;

@Component
public class BeerMapper {

    public BeerSummaryDTO toSummary(Beer beer) {
        return new BeerSummaryDTO(beer.getId(), beer.getName());
    }
  
    public BeerDetailDTO toDetail(Beer beer) {
        return new BeerDetailDTO(beer.getId(), beer.getName(),
            beer.getAbv(), beer.getType(),
            beer.getDescription(),
            beer.getManufacturer().getId());
    }

    public Beer toEntity(BeerCreateDTO dto, Manufacturer manufacturer) {
        Beer beer = new Beer();
        beer.setName(dto.getName());
        beer.setAbv(dto.getAbv());
        beer.setType(dto.getType());
        beer.setDescription(dto.getDescription());
        beer.setManufacturer(manufacturer);
    
        return beer;
    }

}