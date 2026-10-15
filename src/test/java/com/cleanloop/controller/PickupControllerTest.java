package com.cleanloop.controller;

import com.cleanloop.dto.CreatePickupRequest;
import com.cleanloop.dto.UpdatePickupStatusRequest;
import com.cleanloop.entity.PickupStatus;
import com.cleanloop.entity.Role;
import com.cleanloop.entity.User;
import com.cleanloop.entity.WasteCategory;
import com.cleanloop.repository.PickupRequestRepository;
import com.cleanloop.repository.UserRepository;
import com.cleanloop.repository.WasteCategoryRepository;
import com.cleanloop.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PickupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WasteCategoryRepository categoryRepository;

    @Autowired
    private PickupRequestRepository pickupRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User citizen;
    private User authority;
    private String citizenToken;
    private String authorityToken;
    private Long bulkyCategoryId;

    @BeforeEach
    void setUp() {
        tearDown();

        citizen = userRepository.save(new User(
                "pickup.citizen@cleanloop.test",
                passwordEncoder.encode("Secret123!"),
                "Pickup Citizen",
                "+12345600",
                Role.CITIZEN,
                "Central Area"
        ));
        citizenToken = jwtTokenProvider.generateAccessToken(citizen.getId(), citizen.getEmail(), citizen.getRole());

        authority = userRepository.save(new User(
                "pickup.authority@cleanloop.test",
                passwordEncoder.encode("Secret123!"),
                "Supervisor Dave",
                "+12345601",
                Role.AUTHORITY,
                "Central Area"
        ));
        authorityToken = jwtTokenProvider.generateAccessToken(authority.getId(), authority.getEmail(), authority.getRole());

        WasteCategory bulky = categoryRepository.findByCode("BULKY")
                .orElseGet(() -> categoryRepository.save(new WasteCategory("BULKY", "Bulky Waste", "Large items", false)));
        bulkyCategoryId = bulky.getId();
    }

    @AfterEach
    void tearDown() {
        pickupRepository.deleteAll();
        userRepository.findByEmail("pickup.citizen@cleanloop.test").ifPresent(userRepository::delete);
        userRepository.findByEmail("pickup.authority@cleanloop.test").ifPresent(userRepository::delete);
    }

    @Test
    void testPickupLifecycle() throws Exception {
        CreatePickupRequest pickupRequest = new CreatePickupRequest(
                bulkyCategoryId,
                "Old wooden dining table and 4 chairs",
                "45 Elm Street, Apt 3B",
                new BigDecimal("12.97200000"),
                new BigDecimal("77.59500000"),
                "Central Area",
                LocalDate.now().plusDays(2),
                "10:00 - 13:00",
                true,
                "Requires two people to carry downstairs"
        );

        MockMultipartFile pickupPart = new MockMultipartFile(
                "pickup",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(pickupRequest)
        );

        MvcResult createResult = mockMvc.perform(multipart("/api/v1/pickups")
                        .file(pickupPart)
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REQUESTED"))
                .andExpect(jsonPath("$.data.isSpecialHandling").value(true))
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        Long pickupId = objectMapper.readTree(responseJson).path("data").path("id").asLong();

        // 1. Citizen retrieves pickup detail
        mockMvc.perform(get("/api/v1/pickups/" + pickupId)
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(pickupId))
                .andExpect(jsonPath("$.data.status").value("REQUESTED"));

        // 2. Authority updates status to APPROVED
        UpdatePickupStatusRequest approveRequest = new UpdatePickupStatusRequest(
                PickupStatus.APPROVED,
                "Approved for bulky collection on requested date",
                null
        );

        mockMvc.perform(patch("/api/v1/pickups/" + pickupId + "/status")
                        .header("Authorization", "Bearer " + authorityToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        // 3. Citizen lists their pickups
        mockMvc.perform(get("/api/v1/pickups/my")
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void testGetSchedulesPublic() throws Exception {
        mockMvc.perform(get("/api/v1/schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}
