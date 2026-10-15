package com.cleanloop.controller;

import com.cleanloop.entity.Role;
import com.cleanloop.entity.User;
import com.cleanloop.repository.UserRepository;
import com.cleanloop.security.JwtTokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User citizen;
    private User authority;
    private User admin;
    private String citizenToken;
    private String authorityToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        tearDown();

        citizen = userRepository.save(new User(
                "dash.citizen@cleanloop.test",
                passwordEncoder.encode("Pass123!"),
                "Citizen Dash",
                null,
                Role.CITIZEN,
                null
        ));
        citizenToken = jwtTokenProvider.generateAccessToken(citizen.getId(), citizen.getEmail(), citizen.getRole());

        authority = userRepository.save(new User(
                "dash.authority@cleanloop.test",
                passwordEncoder.encode("Pass123!"),
                "Authority Dash",
                null,
                Role.AUTHORITY,
                null
        ));
        authorityToken = jwtTokenProvider.generateAccessToken(authority.getId(), authority.getEmail(), authority.getRole());

        admin = userRepository.save(new User(
                "dash.admin@cleanloop.test",
                passwordEncoder.encode("Pass123!"),
                "Admin Dash",
                null,
                Role.ADMIN,
                null
        ));
        adminToken = jwtTokenProvider.generateAccessToken(admin.getId(), admin.getEmail(), admin.getRole());
    }

    @AfterEach
    void tearDown() {
        userRepository.findByEmail("dash.citizen@cleanloop.test").ifPresent(userRepository::delete);
        userRepository.findByEmail("dash.authority@cleanloop.test").ifPresent(userRepository::delete);
        userRepository.findByEmail("dash.admin@cleanloop.test").ifPresent(userRepository::delete);
    }

    @Test
    void testCitizenDashboardAccess() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/citizen")
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalReports").isNumber());
    }

    @Test
    void testCitizenCannotAccessAuthorityDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/authority")
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAuthorityDashboardAccess() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/authority")
                        .header("Authorization", "Bearer " + authorityToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.submittedReports").isNumber());
    }

    @Test
    void testAdminDashboardAccess() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/admin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalUsers").isNumber())
                .andExpect(jsonPath("$.data.reportsByStatus").isMap());
    }
}
