package com.haufe.beercatalogue.integration;

import com.haufe.beercatalogue.model.Manufacturer;
import com.haufe.beercatalogue.model.User;
import com.haufe.beercatalogue.repository.UserRepository;
import com.haufe.beercatalogue.repository.BeerRepository;
import com.haufe.beercatalogue.repository.ManufacturerRepository;
import com.haufe.beercatalogue.model.Beer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SecurityITest {

    /* Integration test for various role-based restrictions */

    @Autowired private MockMvc mockMvc;
    @Autowired private BeerRepository beerRepository;
    @Autowired private ManufacturerRepository manufacturerRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private Manufacturer savedManufacturer;
    private Beer savedBeer;
    private Beer altBeer;

    @BeforeEach
    void setUp() {
        beerRepository.deleteAll();
        manufacturerRepository.deleteAll();
        userRepository.deleteAll();

        savedManufacturer = manufacturerRepository.save(new Manufacturer("BrewCo", "Germany"));

        Manufacturer altManufacturer = manufacturerRepository.save(new Manufacturer("AltBrew", "Austria"));
        altBeer = beerRepository.save(new Beer("AltBeer", 5.0, "Alt", "A classic alt beer", altManufacturer));
        savedBeer = beerRepository.save(new Beer("IPA", 6.5, "IPA", "Hoppy and fresh", savedManufacturer));

        User manufacturerUser = new User();
        manufacturerUser.setUsername("manufacturer");
        manufacturerUser.setPassword(passwordEncoder.encode("manufacturerpass"));
        manufacturerUser.setManufacturer(savedManufacturer);
        manufacturerUser.setRole("ROLE_MANUFACTURER");
        userRepository.save(manufacturerUser);
    }

    @Test
    void anonymousUser_canAccessBeerGet() throws Exception {
        mockMvc.perform(get("/api/beers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk());
    }

    @Test
    void anonymousUser_canAccessManufacturerGet() throws Exception {
        mockMvc.perform(get("/api/manufacturers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk());
    }

    @Test
    void anonymousUser_cannosAccessBeerPost() throws Exception {
        String json = """
        {
          "name": "IPA One",
          "abv": 6.5,
          "type": "IPA",
          "description": "Hoppy and fresh",
          "manufacturerId": %d
        }
        """.formatted(savedManufacturer.getId());

        mockMvc.perform(post("/api/beers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void manufacturer_canAccessPost() throws Exception {
        String json = """
        {
          "name": "IPA One",
          "abv": 6.5,
          "type": "IPA",
          "description": "Hoppy and fresh",
          "manufacturerId": %d
        }
        """.formatted(savedManufacturer.getId());

        mockMvc.perform(post("/api/beers")
                .with(httpBasic("manufacturer", "manufacturerpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("IPA One"));
    }

    @Test
    void manufacturer_cannotAccessPostForOtherManufacturer() throws Exception {
        String json = """
        {
          "name": "Alt IPA",
          "abv": 6.0,
          "type": "IPA",
          "description": "Hoppy and bitter",
          "manufacturerId": %d
        }
        """.formatted(altBeer.getManufacturer().getId());

        mockMvc.perform(post("/api/beers")
                .with(httpBasic("manufacturer", "manufacturerpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isForbidden());
    }

    @Test
    void manufacturer_cannotPutForOtherManufacturer() throws Exception {
        String json = """
        {
          "name": "Alt IPA",
          "abv": 6.0,
          "type": "IPA",
          "description": "Hoppy and bitter",
          "manufacturerId": %d
        }
        """.formatted(altBeer.getManufacturer().getId());

        mockMvc.perform(put("/api/beers/%d".formatted(savedBeer.getId()))
                .with(httpBasic("manufacturer", "manufacturerpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isForbidden());
    }

    @Test
    void manufacturer_canDeleteOwnBeer() throws Exception {
        mockMvc.perform(delete("/api/beers/%d".formatted(savedBeer.getId()))
                .with(httpBasic("manufacturer", "manufacturerpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNoContent());
    }

    @Test
    void manufacturer_cannotDeleteOtherManufacturerBeer() throws Exception {
        mockMvc.perform(delete("/api/beers/%d".formatted(altBeer.getId()))
                .with(httpBasic("manufacturer", "manufacturerpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden());
    }

}