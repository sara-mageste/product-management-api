package com.saraprojects.product_api.security;

import com.saraprojects.product_api.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class JwtServiceTest {

    private JwtService jwtService;
    private User sampleUser;
    private static final String TEST_SECRET = "fake-jwt-secret-for-testing-purposes-only-32bytes";
    private static final long TEST_EXPIRATION_MS = 1800000; // 30 mins

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", TEST_EXPIRATION_MS);

        sampleUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .employeeCode("EMP-1234")
                .password("secret")
                .avatarId(1)
                .build();
    }

    @Test
    void generateAccessToken_validUser_producesTokenAcceptedByIsTokenValid() {
        String token = jwtService.generateAccessToken(sampleUser);

        assertThat(token).isNotNull().isNotBlank();
        boolean isValid = jwtService.isTokenValid(token);
        assertThat(isValid).isTrue();

        String extractedCode = jwtService.extractEmployeeCode(token);
        assertThat(extractedCode).isEqualTo("EMP-1234");
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        // Set negative expiration to create an immediately expired token
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L);
        String expiredToken = jwtService.generateAccessToken(sampleUser);

        // Reset expiration for verification
        ReflectionTestUtils.setField(jwtService, "expirationMs", TEST_EXPIRATION_MS);

        boolean isValid = jwtService.isTokenValid(expiredToken);
        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenValid_tamperedOrMalformedToken_returnsFalseWithoutUnhandledException() {
        String malformedToken = "invalid.jwt.token.string";
        String tamperedToken = jwtService.generateAccessToken(sampleUser) + "tampered";

        assertThatCode(() -> {
            boolean isMalformedValid = jwtService.isTokenValid(malformedToken);
            assertThat(isMalformedValid).isFalse();

            boolean isTamperedValid = jwtService.isTokenValid(tamperedToken);
            assertThat(isTamperedValid).isFalse();
        }).doesNotThrowAnyException();
    }
}
