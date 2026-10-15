package com.cleanloop.controller;

import com.cleanloop.dto.CreateRecyclingCenterRequest;
import com.cleanloop.entity.Role;
import com.cleanloop.entity.User;
import com.cleanloop.repository.RecyclingCenterRepository;
import com.cleanloop.repository.UserRepository;
import com.cleanloop.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RecyclingCenterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RecyclingCenterRepository centerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;

    @BeforeEach
    void setUp() {
        tearDown();
        User admin = userRepository.save(new User(
                "center.admin@cleanloop.test",
                passwordEncoder.encode("Pass123!"),
                "Admin Center",
                null,
                Role.ADMIN,
                null
        ));
        adminToken = jwtTokenProvider.generateAccessToken(admin.getId(), admin.getEmail(), admin.getRole());
    }

    @AfterEach
    void tearDown() {
        centerRepository.deleteAll();
        userRepository.findByEmail("center.admin@cleanloop.test").ifPresent(userRepository::delete);
    }

    @Test
    void testCreateAndSearchNearbyRecyclingCenters() throws Exception {
        CreateRecyclingCenterRequest centerReq = new CreateRecyclingCenterRequest(
                "Downtown Eco Hub",
                "Community dropoff facility",
                "100 Green Ave",
                new BigDecimal("12.97160000"),
                new BigDecimal("77.59460000"),
                "+91 80 12345678",
                "contact@ecohub.org",
                "08:00 - 18:00",
                "https://ecohub.org",
                "PLASTIC,PAPER,GLASS,METAL"
        );

        // 1. Admin creates center
        mockMvc.perform(post("/api/v1/recycling-centers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(centerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Downtown Eco Hub"))
                .andExpect(jsonPath("$.data.acceptedMaterials").isArray());

        // 2. Public user searches nearby with coordinates
        mockMvc.perform(get("/api/v1/recycling-centers")
                        .param("lat", "12.9715")
                        .param("lng", "77.5945")
                        .param("radiusKm", "10")
                        .param("material", "PLASTIC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].distanceKm").isNumber());
    }
}
