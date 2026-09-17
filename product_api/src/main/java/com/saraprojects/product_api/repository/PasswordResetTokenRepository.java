package com.saraprojects.product_api.repository;

import com.saraprojects.product_api.model.PasswordResetToken;
import com.saraprojects.product_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
}