package com.example.viom16.service;

import com.example.viom16.exception.BusinessValidationException;
import com.example.viom16.exception.EmailAlreadyExistsException;
import com.example.viom16.model.User;
import com.example.viom16.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class UserRegistrationService {
    private static final Set<String> DISALLOWED_DOMAINS = Set.of(
            "tempmail.com",
            "mailinator.com",
            "10minutemail.com"
    );

    private final UserRepository userRepository;

    public UserRegistrationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String email, String password) {
        String normalizedEmail = email.toLowerCase(Locale.ROOT);
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new EmailAlreadyExistsException(email);
        }
        validateBusinessRules(normalizedEmail, password);
        User user = new User(UUID.randomUUID().toString(), normalizedEmail, hashPassword(password));
        return userRepository.save(user);
    }

    private void validateBusinessRules(String email, String password) {
        String domain = email.substring(email.indexOf('@') + 1);
        if (DISALLOWED_DOMAINS.contains(domain)) {
            throw new BusinessValidationException("email domain is not allowed");
        }
        String prefix = email.substring(0, email.indexOf('@'));
        if (password.toLowerCase(Locale.ROOT).contains(prefix.toLowerCase(Locale.ROOT))) {
            throw new BusinessValidationException("password cannot contain email prefix");
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Hash algorithm not available", ex);
        }
    }
}
