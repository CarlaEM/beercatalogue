package com.haufe.beercatalogue.dto;

public class BeerDetailDTO {
    
    private Long id;
    private String name;
    private double abv;
    private String type;
    private String description;
    private Long manufacturerId;

    public BeerDetailDTO(Long id, String name, double abv, String type, String description, Long manufacturerId) {
        this.id = id;
        this.name = name;
        this.abv = abv;
        this.type = type;
        this.description = description;
        this.manufacturerId = manufacturerId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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