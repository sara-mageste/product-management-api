package com.saraprojects.product_api.service;

import com.saraprojects.product_api.dto.*;
import com.saraprojects.product_api.exception.*;
import com.saraprojects.product_api.model.LoginAttempt;
import com.saraprojects.product_api.model.PasswordResetToken;
import com.saraprojects.product_api.model.RefreshToken;
import com.saraprojects.product_api.model.User;
import com.saraprojects.product_api.repository.LoginAttemptRepository;
import com.saraprojects.product_api.repository.PasswordResetTokenRepository;
import com.saraprojects.product_api.repository.UserRepository;
import com.saraprojects.product_api.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoginAttemptRepository loginAttemptRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .employeeCode("EMP-1234")
                .password("encoded-password")
                .avatarId(1)
                .build();
    }

    // ==========================================
    // register() Tests
    // ==========================================

    @Test
    void register_passwordsDoNotMatch_throwsPasswordMismatchException() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
                "Test User",
                "test@example.com",
                "password123",
                "differentPassword",
                1
        );

        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(PasswordMismatchException.class)
                .hasMessage("Passwords do not match");

        verifyNoInteractions(userRepository, emailService);
    }

    @Test
    void register_emailAlreadyExists_throwsEmailAlreadyExistsException() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
                "Test User",
                "existing@example.com",
                "password123",
                "password123",
                1
        );

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already in use");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    @Test
    void register_invalidAvatarId_throwsInvalidRequestException() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
                "Test User",
                "test@example.com",
                "password123",
                "password123",
                99 // Avatar outside 1-8
        );

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);

        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Invalid avatar selected");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    @Test
    void register_successful_generatesEmployeeCodeAndSendsEmail() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
                "Test User",
                "test@example.com",
                "password123",
                "password123",
                3
        );

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByEmployeeCode(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");

        RegisterResponseDTO response = authService.register(dto);

        assertThat(response).isNotNull();
        assertThat(response.employeeCode()).startsWith("EMP-");
        assertThat(response.name()).isEqualTo("Test User");
        assertThat(response.avatarId()).isEqualTo(3);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmployeeCode()).isEqualTo(response.employeeCode());

        verify(emailService).sendEmployeeCodeEmail(
                eq("test@example.com"),
                eq("Test User"),
                eq(response.employeeCode())
        );
    }

    // ==========================================
    // login() Tests
    // ==========================================

    @Test
    void login_nonExistentEmployeeCode_incrementsAttemptAndThrowsInvalidCredentialsException() {
        LoginRequestDTO dto = new LoginRequestDTO("EMP-9999", "wrongPassword");

        when(loginAttemptRepository.findByEmployeeCode("EMP-9999")).thenReturn(Optional.empty());
        when(userRepository.findByEmployeeCode("EMP-9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid employee code or password");

        ArgumentCaptor<LoginAttempt> attemptCaptor = ArgumentCaptor.forClass(LoginAttempt.class);
        verify(loginAttemptRepository).save(attemptCaptor.capture());
        assertThat(attemptCaptor.getValue().getFailedAttempts()).isEqualTo(1);
    }

    @Test
    void login_wrongPassword_incrementsAttemptAndThrowsInvalidCredentialsException() {
        LoginRequestDTO dto = new LoginRequestDTO("EMP-1234", "wrongPassword");

        LoginAttempt attempt = LoginAttempt.builder()
                .employeeCode("EMP-1234")
                .failedAttempts(1)
                .build();

        when(loginAttemptRepository.findByEmployeeCode("EMP-1234")).thenReturn(Optional.of(attempt));
        when(userRepository.findByEmployeeCode("EMP-1234")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongPassword", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid employee code or password");

        ArgumentCaptor<LoginAttempt> attemptCaptor = ArgumentCaptor.forClass(LoginAttempt.class);
        verify(loginAttemptRepository).save(attemptCaptor.capture());
        assertThat(attemptCaptor.getValue().getFailedAttempts()).isEqualTo(2);
    }

    @Test
    void login_invalidCredentials_returnsGenericErrorMessage() {
        LoginRequestDTO nonExistentCodeDto = new LoginRequestDTO("EMP-9999", "wrongPassword");
        LoginRequestDTO wrongPasswordDto = new LoginRequestDTO("EMP-1234", "wrongPassword");

        when(loginAttemptRepository.findByEmployeeCode(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmployeeCode("EMP-9999")).thenReturn(Optional.empty());
        when(userRepository.findByEmployeeCode("EMP-1234")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongPassword", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(nonExistentCodeDto))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageStartingWith("Invalid employee code or password");

        assertThatThrownBy(() -> authService.login(wrongPasswordDto))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageStartingWith("Invalid employee code or password");
    }

    @Test
    void login_wrongPasswordThirdAttempt_locksAccount() {
        LoginRequestDTO dto = new LoginRequestDTO("EMP-1234", "wrongPassword");

        LoginAttempt attempt = LoginAttempt.builder()
                .employeeCode("EMP-1234")
                .failedAttempts(2) // 2 previous failures
                .build();

        when(loginAttemptRepository.findByEmployeeCode("EMP-1234")).thenReturn(Optional.of(attempt));
        when(userRepository.findByEmployeeCode("EMP-1234")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongPassword", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(AccountLockedException.class)
                .hasMessageContaining("Account locked");

        ArgumentCaptor<LoginAttempt> attemptCaptor = ArgumentCaptor.forClass(LoginAttempt.class);
        verify(loginAttemptRepository).save(attemptCaptor.capture());
        assertThat(attemptCaptor.getValue().getFailedAttempts()).isEqualTo(3);
        assertThat(attemptCaptor.getValue().getLockedUntil()).isAfter(LocalDateTime.now());
    }

    @Test
    void login_activeLockout_throwsAccountLockedException() {
        LoginRequestDTO dto = new LoginRequestDTO("EMP-1234", "correctPassword");

        LoginAttempt attempt = LoginAttempt.builder()
                .employeeCode("EMP-1234")
                .failedAttempts(3)
                .lockedUntil(LocalDateTime.now().plusMinutes(5))
                .build();

        when(loginAttemptRepository.findByEmployeeCode("EMP-1234")).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(AccountLockedException.class)
                .hasMessageContaining("Account locked");

        verify(userRepository, never()).findByEmployeeCode(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_successful_resetsFailedAttemptsAndReturnsTokens() {
        LoginRequestDTO dto = new LoginRequestDTO("EMP-1234", "correctPassword");

        LoginAttempt attempt = LoginAttempt.builder()
                .employeeCode("EMP-1234")
                .failedAttempts(2)
                .lockedUntil(null)
                .build();

        RefreshToken refreshToken = RefreshToken.builder()
                .token("fake-refresh-token")
                .user(sampleUser)
                .build();

        when(loginAttemptRepository.findByEmployeeCode("EMP-1234")).thenReturn(Optional.of(attempt));
        when(userRepository.findByEmployeeCode("EMP-1234")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("correctPassword", "encoded-password")).thenReturn(true);
        when(jwtService.generateAccessToken(sampleUser)).thenReturn("fake-access-token");
        when(refreshTokenService.createRefreshToken(sampleUser)).thenReturn(refreshToken);

        AuthResponseDTO response = authService.login(dto);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("fake-access-token");
        assertThat(response.refreshToken()).isEqualTo("fake-refresh-token");

        ArgumentCaptor<LoginAttempt> attemptCaptor = ArgumentCaptor.forClass(LoginAttempt.class);
        verify(loginAttemptRepository).save(attemptCaptor.capture());
        assertThat(attemptCaptor.getValue().getFailedAttempts()).isEqualTo(0);
        assertThat(attemptCaptor.getValue().getLockedUntil()).isNull();
    }

    // ==========================================
    // refresh() Tests
    // ==========================================

    @Test
    void refresh_validToken_rotatesTokenPairAndRevokesOld() {
        RefreshRequestDTO dto = new RefreshRequestDTO("old-refresh-token");

        RefreshToken oldToken = RefreshToken.builder()
                .token("old-refresh-token")
                .user(sampleUser)
                .build();

        RefreshToken newToken = RefreshToken.builder()
                .token("new-refresh-token")
                .user(sampleUser)
                .build();

        when(refreshTokenService.validateAndGet("old-refresh-token")).thenReturn(oldToken);
        when(refreshTokenService.createRefreshToken(sampleUser)).thenReturn(newToken);
        when(jwtService.generateAccessToken(sampleUser)).thenReturn("new-access-token");

        AuthResponseDTO response = authService.refresh(dto);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");

        verify(refreshTokenService).revoke(oldToken);
        verify(refreshTokenService).createRefreshToken(sampleUser);
    }

    @Test
    void refresh_revokedOrExpiredToken_throwsInvalidCredentialsException() {
        RefreshRequestDTO dto = new RefreshRequestDTO("invalid-refresh-token");

        when(refreshTokenService.validateAndGet("invalid-refresh-token"))
                .thenThrow(new InvalidCredentialsException("Invalid or expired refresh token"));

        assertThatThrownBy(() -> authService.refresh(dto))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid or expired refresh token");

        verify(refreshTokenService, never()).revoke(any());
    }

    // ==========================================
    // forgotPassword() Tests
    // ==========================================

    @Test
    void forgotPassword_existingAndNonExistingEmail_returnSameMessage() {
        ForgotPasswordRequestDTO existingDto = new ForgotPasswordRequestDTO("test@example.com");
        ForgotPasswordRequestDTO nonExistingDto = new ForgotPasswordRequestDTO("unknown@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        MessageResponseDTO response1 = authService.forgotPassword(existingDto);
        MessageResponseDTO response2 = authService.forgotPassword(nonExistingDto);

        assertThat(response1.message()).isEqualTo(response2.message());
        assertThat(response1.message())
                .isEqualTo("If that email is registered, a password reset code has been sent to it.");
    }

    @Test
    void forgotPassword_existingEmail_generatesResetTokenAndSendsEmail() {
        ForgotPasswordRequestDTO dto = new ForgotPasswordRequestDTO("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));

        MessageResponseDTO response = authService.forgotPassword(dto);

        assertThat(response).isNotNull();

        verify(passwordResetTokenRepository).deleteByUser(sampleUser);

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());

        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getUser()).isEqualTo(sampleUser);
        assertThat(savedToken.getToken()).isNotNull();
        assertThat(savedToken.isUsed()).isFalse();

        verify(emailService).sendPasswordResetEmail(
                eq("test@example.com"),
                eq("Test User"),
                eq(savedToken.getToken())
        );
    }

    // ==========================================
    // resetPassword() Tests
    // ==========================================

    @Test
    void resetPassword_nonExistentToken_throwsInvalidCredentialsException() {
        ResetPasswordRequestDTO dto = new ResetPasswordRequestDTO(
                "invalid-token",
                "newPassword123",
                "newPassword123"
        );

        when(passwordResetTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(dto))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid or expired reset code");
    }

    @Test
    void resetPassword_expiredToken_throwsInvalidCredentialsException() {
        ResetPasswordRequestDTO dto = new ResetPasswordRequestDTO(
                "expired-token",
                "newPassword123",
                "newPassword123"
        );

        PasswordResetToken expiredToken = PasswordResetToken.builder()
                .token("expired-token")
                .user(sampleUser)
                .expiryDate(LocalDateTime.now().minusMinutes(5))
                .used(false)
                .build();

        when(passwordResetTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> authService.resetPassword(dto))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid or expired reset code");
    }

    @Test
    void resetPassword_alreadyUsedToken_throwsInvalidCredentialsException() {
        ResetPasswordRequestDTO dto = new ResetPasswordRequestDTO(
                "used-token",
                "newPassword123",
                "newPassword123"
        );

        PasswordResetToken usedToken = PasswordResetToken.builder()
                .token("used-token")
                .user(sampleUser)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .used(true)
                .build();

        when(passwordResetTokenRepository.findByToken("used-token")).thenReturn(Optional.of(usedToken));

        assertThatThrownBy(() -> authService.resetPassword(dto))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid or expired reset code");
    }

    @Test
    void resetPassword_passwordsDoNotMatch_throwsPasswordMismatchException() {
        ResetPasswordRequestDTO dto = new ResetPasswordRequestDTO(
                "valid-token",
                "newPassword123",
                "differentPassword"
        );

        assertThatThrownBy(() -> authService.resetPassword(dto))
                .isInstanceOf(PasswordMismatchException.class)
                .hasMessage("Passwords do not match");

        verifyNoInteractions(passwordResetTokenRepository);
    }

    @Test
    void resetPassword_successful_updatesPasswordHashAndRevokesAllRefreshTokens() {
        ResetPasswordRequestDTO dto = new ResetPasswordRequestDTO(
                "valid-token",
                "newPassword123",
                "newPassword123"
        );

        PasswordResetToken validToken = PasswordResetToken.builder()
                .token("valid-token")
                .user(sampleUser)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();

        when(passwordResetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(validToken));
        when(passwordEncoder.encode("newPassword123")).thenReturn("new-encoded-password");

        MessageResponseDTO response = authService.resetPassword(dto);

        assertThat(response).isNotNull();
        assertThat(response.message()).isEqualTo("Password reset successfully. You can now sign in with your new password.");

        verify(userRepository).save(sampleUser);
        assertThat(sampleUser.getPassword()).isEqualTo("new-encoded-password");

        verify(passwordResetTokenRepository).save(validToken);
        assertThat(validToken.isUsed()).isTrue();

        verify(refreshTokenService).revokeAllForUser(sampleUser);
    }
}
