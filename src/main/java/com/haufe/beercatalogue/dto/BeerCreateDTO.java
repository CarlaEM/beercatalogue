package com.haufe.beercatalogue.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BeerCreateDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @Min(value = 0, message = "ABV must be a positive number")
    private double abv;

    @NotBlank(message = "Type is required")
    private String type;

    private String description;

    @NotNull(message = "Manufacturer ID is required")
    private Long manufacturerId;

    public BeerCreateDTO(String name, double abv, String type, String description, Long manufacturerId) {
        this.name = name;
        this.abv = abv;
        this.type = type;
        this.description = description;
        this.manufacturerId = manufacturerId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getAbv() { return abv; }
    public void setAbv(double abv) { this.abv = abv; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getManufacturerId() { return manufacturerId; }
    public void setManufacturerId(Long manufacturerId) { this.manufacturerId = manufacturerId; }
    
}
