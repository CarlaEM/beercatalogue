package com.haufe.beercatalogue.service;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.haufe.beercatalogue.model.User;
import com.haufe.beercatalogue.repository.UserRepository;

@Service
public class ManufacturerAuthorizationService {
    
    /* Service to handle authorization logic for manufacturer operations */

    private final UserRepository userRepository;

    public ManufacturerAuthorizationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Checks if the current user can edit a manufacturer with the specified ID.
     */
    public boolean canEditManufacturer(Long manufacturerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        String username = auth.getName();
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty())
            return false;
        User user = userOptional.get();

        if (user.getRole().equals("ROLE_ADMIN")) {
            return true;
        }

        if (user.getRole().equals("ROLE_MANUFACTURER")) {
            Long userManufacturerId = user.getManufacturer().getId();
            return userManufacturerId != null && userManufacturerId.equals(manufacturerId);
        }

        return false;
    }

}
