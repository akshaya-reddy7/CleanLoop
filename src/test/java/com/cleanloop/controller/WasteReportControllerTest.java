package com.cleanloop.controller;

import com.cleanloop.dto.CreateReportRequest;
import com.cleanloop.dto.RegisterRequest;
import com.cleanloop.dto.UpdateReportStatusRequest;
import com.cleanloop.entity.Role;
import com.cleanloop.entity.User;
import com.cleanloop.entity.WasteReportStatus;
import com.cleanloop.repository.ReportStatusHistoryRepository;
import com.cleanloop.repository.UserRepository;
import com.cleanloop.repository.WasteReportRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WasteReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WasteReportRepository reportRepository;

    @Autowired
    private ReportStatusHistoryRepository historyRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User citizenUser;
    private User authorityUser;
    private User otherCitizenUser;
    private String citizenToken;
    private String authorityToken;
    private String otherCitizenToken;

    @BeforeEach
    void setUp() {
        tearDown();

        citizenUser = userRepository.save(new User(
                "report.citizen@cleanloop.test",
                passwordEncoder.encode("Pass123!"),
                "Report Citizen",
                "+12345678",
                Role.CITIZEN,
                "North District"
        ));
        citizenToken = jwtTokenProvider.generateAccessToken(citizenUser.getId(), citizenUser.getEmail(), citizenUser.getRole());

        authorityUser = userRepository.save(new User(
                "report.authority@cleanloop.test",
                passwordEncoder.encode("Pass123!"),
                "Officer Jones",
                "+12345679",
                Role.AUTHORITY,
                "North District"
        ));
        authorityToken = jwtTokenProvider.generateAccessToken(authorityUser.getId(), authorityUser.getEmail(), authorityUser.getRole());

        otherCitizenUser = userRepository.save(new User(
                "other.citizen@cleanloop.test",
                passwordEncoder.encode("Pass123!"),
                "Other Citizen",
                "+12345670",
                Role.CITIZEN,
                "South District"
        ));
        otherCitizenToken = jwtTokenProvider.generateAccessToken(otherCitizenUser.getId(), otherCitizenUser.getEmail(), otherCitizenUser.getRole());
    }

    @AfterEach
    void tearDown() {
        reportRepository.deleteAll();
        userRepository.findByEmail("report.citizen@cleanloop.test").ifPresent(userRepository::delete);
        userRepository.findByEmail("report.authority@cleanloop.test").ifPresent(userRepository::delete);
        userRepository.findByEmail("other.citizen@cleanloop.test").ifPresent(userRepository::delete);
    }

    @Test
    void testGetCategoriesPublic() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].code").isNotEmpty());
    }

    @Test
    void testCreateReportAndRetrieveLifecycle() throws Exception {
        CreateReportRequest reportRequest = new CreateReportRequest(
                "Overflowing Bin on Main St",
                "Bin is completely full and spilling over sidewalk",
                "OVERFLOWING_BIN",
                null,
                new BigDecimal("12.97160000"),
                new BigDecimal("77.59460000"),
                "123 Main St",
                "Downtown"
        );

        MockMultipartFile reportPart = new MockMultipartFile(
                "report",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(reportRequest)
        );

        MockMultipartFile imagePart = new MockMultipartFile(
                "images",
                "bin.jpg",
                "image/jpeg",
                "dummy image content bytes".getBytes()
        );

        MvcResult createResult = mockMvc.perform(multipart("/api/v1/reports")
                        .file(reportPart)
                        .file(imagePart)
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Overflowing Bin on Main St"))
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.data.images").isArray())
                .andExpect(jsonPath("$.data.images.length()").value(1))
                .andExpect(jsonPath("$.data.statusHistory").isArray())
                .andReturn();

        String responseContent = createResult.getResponse().getContentAsString();
        Long reportId = objectMapper.readTree(responseContent).path("data").path("id").asLong();

        // 1. Citizen retrieves own report
        mockMvc.perform(get("/api/v1/reports/" + reportId)
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(reportId));

        // 2. Other citizen tries to view -> 403 Forbidden
        mockMvc.perform(get("/api/v1/reports/" + reportId)
                        .header("Authorization", "Bearer " + otherCitizenToken))
                .andExpect(status().isForbidden());

        // 3. Authority views report -> 200 OK
        mockMvc.perform(get("/api/v1/reports/" + reportId)
                        .header("Authorization", "Bearer " + authorityToken))
                .andExpect(status().isOk());

        // 4. Authority transitions status to UNDER_REVIEW
        UpdateReportStatusRequest statusRequest = new UpdateReportStatusRequest(
                WasteReportStatus.UNDER_REVIEW,
                "Under review by North District waste management team",
                null
        );

        mockMvc.perform(patch("/api/v1/reports/" + reportId + "/status")
                        .header("Authorization", "Bearer " + authorityToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UNDER_REVIEW"))
                .andExpect(jsonPath("$.data.statusHistory.length()").value(2));

        // 5. Citizen lists their own reports
        mockMvc.perform(get("/api/v1/reports/my")
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(reportId));
    }
}
