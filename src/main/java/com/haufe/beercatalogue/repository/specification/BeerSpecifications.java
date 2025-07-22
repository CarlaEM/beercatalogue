package com.haufe.beercatalogue.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import com.haufe.beercatalogue.model.Beer;

public class BeerSpecifications {

    /* Filtering for beer queries */

    public static Specification<Beer> hasNameLike(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) return null;
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Beer> hasTypeLike(String type) {
        return (root, query, cb) -> {
            if (type == null || type.isBlank()) return null;
            return cb.like(cb.lower(root.get("type")), "%" + type.toLowerCase() + "%");
        };
    }

    public static Specification<Beer> hasManufacturerId(Long manufacturerId) {
        return (root, query, cb) -> {
            if (manufacturerId == null) return null;
            return cb.equal(root.get("manufacturer").get("id"), manufacturerId);
        };
    }

    public static Specification<Beer> hasAbv(Double abv) {
        return (root, query, cb) -> {
            if (abv == null) return null;
            return cb.equal(root.get("abv"), abv);
        };
    }
}