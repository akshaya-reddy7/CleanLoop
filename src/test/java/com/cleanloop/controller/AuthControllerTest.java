package com.cleanloop.controller;

import com.cleanloop.dto.LoginRequest;
import com.cleanloop.dto.RegisterRequest;
import com.cleanloop.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.findByEmail("test.citizen@cleanloop.test").ifPresent(userRepository::delete);
    }

    @Test
    void testRegisterCitizenSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "test.citizen@cleanloop.test",
                "Password123!",
                "Citizen Tester",
                "+1234567890"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test.citizen@cleanloop.test"))
                .andExpect(jsonPath("$.data.role").value("CITIZEN"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    void testRegisterDuplicateEmailFailsWithConflict() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "test.citizen@cleanloop.test",
                "Password123!",
                "Citizen Tester",
                "+1234567890"
        );

        // First registration
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Second registration with same email
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("already registered")));
    }

    @Test
    void testLoginSuccessAndGetProfile() throws Exception {
        RegisterRequest regRequest = new RegisterRequest(
                "test.citizen@cleanloop.test",
                "Password123!",
                "Citizen Tester",
                "+1234567890"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest(
                "test.citizen@cleanloop.test",
                "Password123!"
        );

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseContent).path("data").path("accessToken").asText();

        // Access protected profile endpoint with token
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("test.citizen@cleanloop.test"))
                .andExpect(jsonPath("$.data.fullName").value("Citizen Tester"))
                .andExpect(jsonPath("$.data.role").value("CITIZEN"));
    }

    @Test
    void testLoginWithInvalidPasswordFails() throws Exception {
        RegisterRequest regRequest = new RegisterRequest(
                "test.citizen@cleanloop.test",
                "Password123!",
                "Citizen Tester",
                "+1234567890"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        LoginRequest badLogin = new LoginRequest(
                "test.citizen@cleanloop.test",
                "WrongPassword!"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
