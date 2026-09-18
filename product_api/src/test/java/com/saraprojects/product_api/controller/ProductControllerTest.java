package com.saraprojects.product_api.controller;

import com.saraprojects.product_api.model.User;
import com.saraprojects.product_api.repository.UserRepository;
import com.saraprojects.product_api.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        sampleUser = User.builder()
                .name("Protected User")
                .email("protected@example.com")
                .employeeCode("EMP-5001")
                .password(passwordEncoder.encode("password123"))
                .avatarId(1)
                .build();

        userRepository.save(sampleUser);
    }

    @Test
    void getProducts_withoutAuthorizationHeader_returns401Or403() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status == 401 || status == 403 : "Expected 401 or 403, but got " + status;
                });
    }

    @Test
    void getProducts_withValidJwtToken_returns200Ok() throws Exception {
        String validToken = jwtService.generateAccessToken(sampleUser);

        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }

    @Test
    void getProducts_withTamperedJwtToken_returns401Or403() throws Exception {
        String validToken = jwtService.generateAccessToken(sampleUser);
        String tamperedToken = validToken + "invalidSignature";

        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + tamperedToken))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status == 401 || status == 403 : "Expected 401 or 403, but got " + status;
                });
    }
}
