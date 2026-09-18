package com.saraprojects.product_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saraprojects.product_api.dto.LoginRequestDTO;
import com.saraprojects.product_api.dto.RegisterRequestDTO;
import com.saraprojects.product_api.model.User;
import com.saraprojects.product_api.repository.LoginAttemptRepository;
import com.saraprojects.product_api.repository.UserRepository;
import com.saraprojects.product_api.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginAttemptRepository loginAttemptRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private EmailService emailService;

    private User registeredUser;

    @BeforeEach
    void setUp() {
        loginAttemptRepository.deleteAll();
        userRepository.deleteAll();

        registeredUser = User.builder()
                .name("Alice Smith")
                .email("alice@example.com")
                .employeeCode("EMP-1001")
                .password(passwordEncoder.encode("password123"))
                .avatarId(1)
                .build();

        userRepository.save(registeredUser);
    }

    // ==========================================
    // POST /api/auth/register Integration Tests
    // ==========================================

    @Test
    void register_validPayload_returns201Created() throws Exception {
        RegisterRequestDTO dto = new RegisterRequestDTO(
                "Bob Johnson",
                "bob@example.com",
                "Password123!",
                "Password123!",
                2
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeCode", notNullValue()))
                .andExpect(jsonPath("$.name").value("Bob Johnson"))
                .andExpect(jsonPath("$.avatarId").value(2));
    }

    @Test
    void register_mismatchedPasswords_returns400BadRequest() throws Exception {
        RegisterRequestDTO dto = new RegisterRequestDTO(
                "Bob Johnson",
                "bob2@example.com",
                "Password123!",
                "DifferentPassword!",
                2
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Passwords do not match"));
    }

    @Test
    void register_invalidAvatarId_returns400BadRequest() throws Exception {
        RegisterRequestDTO dto = new RegisterRequestDTO(
                "Bob Johnson",
                "bob3@example.com",
                "Password123!",
                "Password123!",
                99 // Avatar outside allowed range 1-8
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid avatar selected"));
    }

    @Test
    void register_duplicateEmail_returns409Conflict() throws Exception {
        RegisterRequestDTO dto = new RegisterRequestDTO(
                "Alice Copy",
                "alice@example.com", // Already existing email
                "Password123!",
                "Password123!",
                1
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already in use"));
    }

    // ==========================================
    // POST /api/auth/login Integration Tests
    // ==========================================

    @Test
    void login_validCredentials_returns200Ok() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO("EMP-1001", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.employeeCode").value("EMP-1001"));
    }

    @Test
    void login_wrongPassword_returns401Unauthorized() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO("EMP-1001", "wrongPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("Invalid employee code or password")));
    }

    @Test
    void login_nonExistentEmployeeCode_returns401Unauthorized() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO("EMP-9999", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("Invalid employee code or password")));
    }

    @Test
    void login_thirdFailedAttempt_returns423Locked() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO("EMP-1001", "wrongPassword");

        // Attempt 1 -> 401
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());

        // Attempt 2 -> 401
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());

        // Attempt 3 -> 423 Locked
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.message", containsString("Account locked")));
    }
}
