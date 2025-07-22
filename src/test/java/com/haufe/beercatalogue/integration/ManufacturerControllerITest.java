package com.haufe.beercatalogue.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Assertions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import com.haufe.beercatalogue.model.Manufacturer;
import com.haufe.beercatalogue.repository.BeerRepository;
import com.haufe.beercatalogue.repository.ManufacturerRepository;
import com.haufe.beercatalogue.repository.UserRepository;
import com.haufe.beercatalogue.model.User;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ManufacturerControllerITest {

    /* Integration test for manufacturer endpoints */

    @Autowired private MockMvc mockMvc;
    @Autowired private BeerRepository beerRepository;
    @Autowired private ManufacturerRepository manufacturerRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDb() {
        beerRepository.deleteAll();
        manufacturerRepository.deleteAll();
        userRepository.deleteAll();

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("adminpass"));
        admin.setRole("ROLE_ADMIN");
        userRepository.save(admin);
    }

    @Test
    void createManufacturer_ReturnsCreated() throws Exception {
        String json = """
        {
          "name": "CraftHaus",
          "country": "Austria"
        }
        """;

        mockMvc.perform(post("/api/manufacturers")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("CraftHaus"));
    }

    @Test
    void getList_ReturnsManufacturers() throws Exception {
        manufacturerRepository.save(new Manufacturer("B1", "US"));
        manufacturerRepository.save(new Manufacturer("B2", "UK"));

        mockMvc.perform(get("/api/manufacturers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void getById_ReturnsDetails() throws Exception {
        Manufacturer m = manufacturerRepository.save(new Manufacturer("BrewTeam", "Spain"));

        mockMvc.perform(get("/api/manufacturers/" + m.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.country").value("Spain"));
    }

    @Test
    void delete_RemovesManufacturer() throws Exception {
        Manufacturer m = manufacturerRepository.save(new Manufacturer("ToDelete", "France"));

        mockMvc.perform(delete("/api/manufacturers/" + m.getId())
                .with(httpBasic("admin", "adminpass")))
            .andExpect(status().isNoContent());

        Assertions.assertTrue(manufacturerRepository.findById(m.getId()).isEmpty());
    }

    @Test
    void createManufacturer_ReturnsBadRequest_WhenNameIsMissing() throws Exception {
        String json = """
        {
        "country": "USA"
        }
        """;

        mockMvc.perform(post("/api/manufacturers")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createManufacturer_ReturnsBadRequest_WhenCountryIsBlank() throws Exception {
        String json = """
        {
        "name": "BlankCountry",
        "country": ""
        }
        """;

        mockMvc.perform(post("/api/manufacturers")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }
}