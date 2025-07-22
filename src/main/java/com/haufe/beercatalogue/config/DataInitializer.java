package com.haufe.beercatalogue.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.haufe.beercatalogue.repository.BeerRepository;
import com.haufe.beercatalogue.repository.ManufacturerRepository;
import com.haufe.beercatalogue.repository.UserRepository;
import com.haufe.beercatalogue.model.Manufacturer;
import com.haufe.beercatalogue.model.User;
import com.haufe.beercatalogue.model.Beer;

@Configuration
public class DataInitializer {
    
    // Create some initial users or roles if needed
    @Bean
    public CommandLineRunner seedData(
            ManufacturerRepository manufacturerRepository,
            BeerRepository beerRepository,
            UserRepository userRepository,
            PasswordEncoder encoder) {
        return args -> {
            // 1. Create manufacturers
            Manufacturer m1 = manufacturerRepository.save(new Manufacturer("Brew Co", "US"));
            Manufacturer m2 = manufacturerRepository.save(new Manufacturer("Craft Works", "UK"));

            // 2. Create beers linked to manufacturers
            beerRepository.save(new Beer("IPA", 5.5, "India Pale Ale", "", m1));
            beerRepository.save(new Beer("Stout", 6.0, "Dark Ale", "", m2));
            beerRepository.save(new Beer("Pilsner", 4.5, "Light Lager", "", m1));
            beerRepository.save(new Beer("Porter", 5.8, "Dark Ale", "", m2));

            // 2. Create users linked to manufacturers
            if (userRepository.findByUsername("brewery1").isEmpty()) {
                userRepository.save(new User("brewery1", encoder.encode("brewpass"), "ROLE_MANUFACTURER", m1));
            }

            if (userRepository.findByUsername("brewery2").isEmpty()) {
                userRepository.save(new User("brewery2", encoder.encode("brewpass2"), "ROLE_MANUFACTURER", m2));
            }

            // 3. Admin and read-only users
            if (userRepository.findByUsername("admin").isEmpty()) {
                userRepository.save(new User("admin", encoder.encode("adminpass"), "ROLE_ADMIN", null));
            }
        };
    }

}
