package com.saraprojects.product_api.service;

import com.saraprojects.product_api.dto.*;
import com.saraprojects.product_api.exception.AccountLockedException;
import com.saraprojects.product_api.exception.EmailAlreadyExistsException;
import com.saraprojects.product_api.exception.InvalidCredentialsException;
import com.saraprojects.product_api.exception.InvalidRequestException;
import com.saraprojects.product_api.exception.PasswordMismatchException;
import com.saraprojects.product_api.model.LoginAttempt;
import com.saraprojects.product_api.model.PasswordResetToken;
import com.saraprojects.product_api.model.RefreshToken;
import com.saraprojects.product_api.model.User;
import com.saraprojects.product_api.repository.LoginAttemptRepository;
import com.saraprojects.product_api.repository.PasswordResetTokenRepository;
import com.saraprojects.product_api.repository.UserRepository;
import com.saraprojects.product_api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private static final int MAX_ATTEMPTS = 3;
    private static final int LOCK_DURATION_MINUTES = 5;
    private static final int RESET_TOKEN_EXPIRATION_MINUTES = 30;

    private static final Set<Integer> ALLOWED_AVATAR_IDS = Set.of(1, 2, 3, 4, 5, 6, 7, 8);

    private final UserRepository userRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    public RegisterResponseDTO register(RegisterRequestDTO dto) {

        if (!dto.password().equals(dto.confirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }

        if (userRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException("Email already in use");
        }

        if (!ALLOWED_AVATAR_IDS.contains(dto.avatarId())) {
            throw new InvalidRequestException("Invalid avatar selected");
        }

        String employeeCode = generateUniqueEmployeeCode();

        User user = User.builder()
                .name(dto.name())
                .email(dto.email())
                .employeeCode(employeeCode)
                .password(passwordEncoder.encode(dto.password()))
                .avatarId(dto.avatarId())
                .build();

        userRepository.save(user);

        emailService.sendEmployeeCodeEmail(user.getEmail(), user.getName(), employeeCode);

        return new RegisterResponseDTO(
                employeeCode,
                user.getName(),
                user.getAvatarId(),
                "Account created successfully! Your employee code has also been sent to your email."
        );
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {

        LoginAttempt attempt = loginAttemptRepository.findByEmployeeCode(dto.employeeCode())
                .orElseGet(() -> LoginAttempt.builder()
                        .employeeCode(dto.employeeCode())
                        .failedAttempts(0)
                        .build());

        LocalDateTime now = LocalDateTime.now();

        if (attempt.getLockedUntil() != null) {
            if (attempt.getLockedUntil().isAfter(now)) {
                throw new AccountLockedException(buildLockMessage(attempt.getLockedUntil(), now));
            }
            attempt.setFailedAttempts(0);
            attempt.setLockedUntil(null);
        }

        Optional<User> userOpt = userRepository.findByEmployeeCode(dto.employeeCode());

        boolean passwordMatches = userOpt.isPresent()
        && passwordEncoder.matches(dto.password(),  userOpt.get().getPassword());

        if (!passwordMatches) {
            registerFailedAttempt(attempt, now);
        }

        attempt.setFailedAttempts(0);
        attempt.setLockedUntil(null);
        loginAttemptRepository.save(attempt);

        User user = userOpt.get();

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponseDTO(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                user.getEmployeeCode(),
                user.getName(),
                user.getAvatarId()
        );
    }

    public AuthResponseDTO refresh(RefreshRequestDTO dto) {

        RefreshToken oldToken = refreshTokenService.validateAndGet(dto.refreshToken());
        User user = oldToken.getUser();

        refreshTokenService.revoke(oldToken);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        String newAccessToken = jwtService.generateAccessToken(user);

        return new AuthResponseDTO(
                newAccessToken,
                newRefreshToken.getToken(),
                "Bearer",
                user.getEmployeeCode(),
                user.getName(),
                user.getAvatarId()
        );
    }

    public void logout(RefreshRequestDTO dto) {
        RefreshToken token = refreshTokenService.validateAndGet(dto.refreshToken());
        refreshTokenService.revoke(token);
    }

    public MessageResponseDTO forgotPassword(ForgotPasswordRequestDTO dto) {

        Optional<User> userOpt = userRepository.findByEmail(dto.email());

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            passwordResetTokenRepository.deleteByUser(user);

            String token = UUID.randomUUID().toString();

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiryDate(LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRATION_MINUTES))
                    .used(false)
                    .build();

            passwordResetTokenRepository.save(resetToken);

            emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), token);
        }

        return new MessageResponseDTO(
                "If that email is registered, a password reset code has been sent to it."
        );
    }

    public MessageResponseDTO resetPassword(ResetPasswordRequestDTO dto) {

        if (!dto.newPassword().equals(dto.confirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(dto.token())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid or expired reset code"));

        if (resetToken.isUsed() || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidCredentialsException("Invalid or expired reset code");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        refreshTokenService.revokeAllForUser(user);

        return new MessageResponseDTO("Password reset successfully. You can now sign in with your new password.");
    }

    private void registerFailedAttempt(LoginAttempt attempt, LocalDateTime now) {
        int attempts = attempt.getFailedAttempts() + 1;
        attempt.setFailedAttempts(attempts);

        if (attempts >= MAX_ATTEMPTS) {
            LocalDateTime lockedUntil = now.plusMinutes(LOCK_DURATION_MINUTES);
            attempt.setLockedUntil(lockedUntil);
            loginAttemptRepository.save(attempt);
            throw new AccountLockedException(buildLockMessage(lockedUntil, now));
        }

        loginAttemptRepository.save(attempt);
        int remaining = MAX_ATTEMPTS - attempts;
        throw new InvalidCredentialsException(
                "Invalid employee code or password. " + remaining + " attempt(s) remaining before lockout."
        );
    }

    private String generateUniqueEmployeeCode() {
        String code;
        do {
            int number = 1000 + secureRandom.nextInt(9000);
            code = "EMP-" + number;
        } while (userRepository.existsByEmployeeCode(code));

        return code;
    }

    private String buildLockMessage(LocalDateTime lockedUntil, LocalDateTime now) {
        long totalSeconds = Duration.between(now, lockedUntil).getSeconds();
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        if (minutes > 0) {
            return "Account locked. Try again in " + minutes + "m " + seconds + "s.";
        }
        return "Account locked. Try again in " + seconds + "s.";
    }
}