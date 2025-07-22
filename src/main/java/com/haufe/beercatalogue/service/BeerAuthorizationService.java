package com.haufe.beercatalogue.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.haufe.beercatalogue.dto.BeerCreateDTO;
import com.haufe.beercatalogue.model.Beer;
import com.haufe.beercatalogue.model.Manufacturer;
import com.haufe.beercatalogue.model.User;
import com.haufe.beercatalogue.repository.BeerRepository;
import com.haufe.beercatalogue.repository.UserRepository;

@Service
public class BeerAuthorizationService {
 
    /* Service to handle authorization logic for beer operations */

    private final UserRepository userRepository;
    private final BeerRepository beerRepository;

    public BeerAuthorizationService(UserRepository userRepository, BeerRepository beerRepository) {
        this.userRepository = userRepository;
        this.beerRepository = beerRepository;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        
        String username = auth.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * Checks if the current user can create a beer for the specified manufacturer.
     */
    public boolean canCreateBeer(Long manufacturerId) {
        User user = getCurrentUser();
        if (user == null)
            return false;

        // Check if user is admin
        if (user.getRole().equals("ROLE_ADMIN")) {
            return true;
        }

        // Check if user is the manufacturer of this beer
        if (user.getRole().equals("ROLE_MANUFACTURER")) {
            return user.getManufacturer() != null &&
                   user.getManufacturer().getId() != null &&
                   user.getManufacturer().getId().equals(manufacturerId);
        }
        
        return false;
    }


    /**
     * Checks if the current user can edit a beer with the specified ID.
     */
    public boolean canEditBeer(Long beerId, BeerCreateDTO beerCreateDTO) {
        User user = getCurrentUser();
        if (user == null)
            return false;

        // Check if user is admin
        if (user.getRole().equals("ROLE_ADMIN")) {
            return true;
        }

        // Check if user is the manufacturer of this beer
        if (user.getRole().equals("ROLE_MANUFACTURER")) {
            Long userManufacturerId = user.getManufacturer().getId();
            Long beerManufacturerId = beerRepository.findById(beerId)
                .map(Beer::getManufacturer)
                .map(Manufacturer::getId)
                .orElse(null);

            return userManufacturerId != null &&
                userManufacturerId.equals(beerManufacturerId) &&
                beerCreateDTO != null && beerCreateDTO.getManufacturerId() != null &&
                beerCreateDTO.getManufacturerId().equals(userManufacturerId);
        }

        // For anonymous or other roles, no edit permission
        return false;
    }

    public boolean canDeleteBeer(Long beerId) {
        User user = getCurrentUser();
        if (user == null)
            return false;

        // Check if user is admin
        if (user.getRole().equals("ROLE_ADMIN")) {
            return true;
        }

        // Check if user is the manufacturer of this beer
        if (user.getRole().equals("ROLE_MANUFACTURER")) {
            Long userManufacturerId = user.getManufacturer().getId();
            Long beerManufacturerId = beerRepository.findById(beerId)
                .map(Beer::getManufacturer)
                .map(Manufacturer::getId)
                .orElse(null);

            return userManufacturerId != null && userManufacturerId.equals(beerManufacturerId);
        }

        return false;
    }

}
