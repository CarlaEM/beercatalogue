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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import com.haufe.beercatalogue.model.Beer;
import com.haufe.beercatalogue.model.Manufacturer;
import com.haufe.beercatalogue.model.User;
import com.haufe.beercatalogue.repository.BeerRepository;
import com.haufe.beercatalogue.repository.ManufacturerRepository;
import com.haufe.beercatalogue.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BeerControllerITest {

    /* Integration test for beer endpoints */

    @Autowired private MockMvc mockMvc;
    @Autowired private BeerRepository beerRepository;
    @Autowired private ManufacturerRepository manufacturerRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private Manufacturer savedManufacturer;

    @BeforeEach
    void setUp() {
        beerRepository.deleteAll();
        manufacturerRepository.deleteAll();
        savedManufacturer = manufacturerRepository.save(new Manufacturer("BrewCo", "Germany"));
        
        userRepository.deleteAll();
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("adminpass"));
        admin.setRole("ROLE_ADMIN");
        userRepository.save(admin);
    }

    @Test
    void getAll_ReturnsEmptyList_WhenNoBeersExist() throws Exception {
        mockMvc.perform(get("/api/beers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void createBeer_ReturnsCreatedBeer() throws Exception {
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
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("IPA One"));
    }

    @Test
    void getById_ReturnsBeerDetail() throws Exception {
        Beer beer = beerRepository.save(new Beer("Test", 5.0, "Lager", "Smooth", savedManufacturer));

        mockMvc.perform(get("/api/beers/" + beer.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Test"));
    }

    @Test
    void deleteBeer_RemovesBeer() throws Exception {
        Beer beer = beerRepository.save(new Beer("ToDelete", 4.0, "Stout", "Dark", savedManufacturer));

        mockMvc.perform(delete("/api/beers/" + beer.getId())
                .with(httpBasic("admin", "adminpass")))
            .andExpect(status().isNoContent());

        Assertions.assertTrue(beerRepository.findById(beer.getId()).isEmpty());
    }

    @Test
    void createBeer_ReturnsBadRequest_WhenNameMissing() throws Exception {
        String json = """
        {
        "abv": 5.0,
        "type": "IPA",
        "description": "Nice",
        "manufacturerId": %d
        }
        """.formatted(savedManufacturer.getId());

        mockMvc.perform(post("/api/beers")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createBeer_ReturnsBadRequest_WhenAbvNegative() throws Exception {
        String json = """
        {
        "name": "Bad ABV",
        "abv": -2.0,
        "type": "Stout",
        "description": "Invalid",
        "manufacturerId": %d
        }
        """.formatted(savedManufacturer.getId());

        mockMvc.perform(post("/api/beers")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getList_ReturnsBeersSortedByName_Asc() throws Exception {
        beerRepository.save(new Beer("Zebra", 5.1, "IPA", "Z", savedManufacturer));
        beerRepository.save(new Beer("Alpha", 5.2, "IPA", "A", savedManufacturer));

        mockMvc.perform(get("/api/beers?sortBy=name&dir=asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("Alpha"))
            .andExpect(jsonPath("$.content[1].name").value("Zebra"));
    }

    @Test
    void getList_ReturnsBeersSortedByName_Desc() throws Exception {
        beerRepository.save(new Beer("Alpha", 5.1, "IPA", "A", savedManufacturer));
        beerRepository.save(new Beer("Zebra", 5.2, "IPA", "Z", savedManufacturer));

        mockMvc.perform(get("/api/beers?sortBy=name&dir=desc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("Zebra"))
            .andExpect(jsonPath("$.content[1].name").value("Alpha"));
    }

    @Test
    void getList_ReturnsBadRequest_WhenSortByInvalid() throws Exception {
        mockMvc.perform(get("/api/beers?sortBy=invalidField&dir=asc"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getList_ReturnsBadRequest_WhenDirectionInvalid() throws Exception {
        mockMvc.perform(get("/api/beers?sortBy=name&dir=upwards"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getList_FilterByName_ReturnsMatchingBeers() throws Exception {
        beerRepository.save(new Beer("Alpha", 5.0, "IPA", "Light", savedManufacturer));
        beerRepository.save(new Beer("Bravo", 6.0, "Stout", "Strong", savedManufacturer));

        mockMvc.perform(get("/api/beers?name=alp"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].name").value("Alpha"));
    }

    @Test
    void getList_FilterByType_ReturnsMatchingBeers() throws Exception {
        beerRepository.save(new Beer("Lager Light", 4.5, "Lager", "Crisp and clean", savedManufacturer));
        beerRepository.save(new Beer("IPA Storm", 6.0, "IPA", "Hoppy and bitter", savedManufacturer));

        mockMvc.perform(get("/api/beers?type=ipa"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].name").value("IPA Storm"));
    }

    @Test
    void getList_FilterByAbv_ReturnsMatchingBeers() throws Exception {
        beerRepository.save(new Beer("Session Pale", 4.0, "Pale Ale", "Light", savedManufacturer));
        beerRepository.save(new Beer("Imperial Stout", 9.0, "Stout", "Strong and rich", savedManufacturer));

        mockMvc.perform(get("/api/beers?abv=9.0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].name").value("Imperial Stout"));
    }

    @Test
    void getList_FilterByManufacturer_ReturnsMatchingBeers() throws Exception {
        Manufacturer anotherManufacturer = manufacturerRepository.save(new Manufacturer("OtherBrew", "Italy"));

        beerRepository.save(new Beer("Main Brew", 5.5, "IPA", "Fruity", savedManufacturer));
        beerRepository.save(new Beer("Other Brew", 4.5, "Pilsner", "Light", anotherManufacturer));

        mockMvc.perform(get("/api/beers?manufacturerId=" + savedManufacturer.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].name").value("Main Brew"));
    }

}