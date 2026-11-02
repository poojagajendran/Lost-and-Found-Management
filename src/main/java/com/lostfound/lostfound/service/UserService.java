package com.lostfound.lostfound.service;

import com.lostfound.lostfound.dto.UserRegistrationDto;
import com.lostfound.lostfound.model.User;
import com.lostfound.lostfound.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(UserRegistrationDto dto) {
        String normalizedEmail = dto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("An account with email " + normalizedEmail + " already exists.");
        }

        if (dto.getConfirmPassword() != null && !dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        User user = new User();
        user.setName(dto.getName().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole("ROLE_USER");

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email.trim().toLowerCase());
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        if (email == null) {
            return false;
        }
        return userRepository.existsByEmail(email.trim().toLowerCase());
    }

    @Transactional
    public User ensureAdminUser(String email, String rawPassword, String name) {
        String normalizedEmail = email.trim().toLowerCase();
        return userRepository.findByEmail(normalizedEmail).orElseGet(() -> {
            User admin = new User();
            admin.setName(name);
            admin.setEmail(normalizedEmail);
            admin.setPasswordHash(passwordEncoder.encode(rawPassword));
            admin.setRole("ROLE_ADMIN");
            return userRepository.save(admin);
        });
    }
}
