package com.haufe.beercatalogue.mapper;

import org.springframework.stereotype.Component;

import com.haufe.beercatalogue.dto.ManufacturerCreateDTO;
import com.haufe.beercatalogue.dto.ManufacturerDetailDTO;
import com.haufe.beercatalogue.model.Manufacturer;

@Component
public class ManufacturerMapper {

    public ManufacturerDetailDTO toDetail(Manufacturer manufacturer) {
        ManufacturerDetailDTO dto = new ManufacturerDetailDTO(manufacturer.getId(), manufacturer.getName(), manufacturer.getCountry());
        return dto;
    }

    public Manufacturer toEntity(ManufacturerCreateDTO dto) {
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setName(dto.getName());
        manufacturer.setCountry(dto.getCountry());
        return manufacturer;
    }

}