package com.haufe.beercatalogue.model;

import jakarta.persistence.*;

@Entity
public class Manufacturer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String country;

    public Manufacturer() {}

    public Manufacturer(long id, String name, String country) {
        this.id = id;
        this.name = name;
        this.country = country;
    }

    public Manufacturer(String name, String country) {
        this.name = name;
        this.country = country;
    }   

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
