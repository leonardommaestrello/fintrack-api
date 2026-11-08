
package com.fintrack.api.auth.service.impl;

import com.fintrack.api.auth.dto.LoginRequest;
import com.fintrack.api.auth.dto.LoginResponse;
import com.fintrack.api.auth.dto.RegisterRequest;
import com.fintrack.api.auth.dto.RegisterResponse;
import com.fintrack.api.auth.service.AuthService;
import com.fintrack.api.category.service.DefaultCategoryFactory;
import com.fintrack.api.common.exception.EmailAlreadyExistsException;
import com.fintrack.api.common.exception.InvalidCredentialsException;
import com.fintrack.api.security.jwt.JwtService;
import com.fintrack.api.user.entity.User;
import com.fintrack.api.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final DefaultCategoryFactory defaultCategoryFactory;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        String email = request.email()
                .trim()
                .toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(
                    "Email is already registered"
            );
        }

        if (request.password()
                .getBytes(StandardCharsets.UTF_8).length > 72) {

            throw new IllegalArgumentException(
                    "Password exceeds the 72-byte BCrypt limit"
            );
        }

        User user = new User();

        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );

        User savedUser = userRepository.save(user);

        defaultCategoryFactory.createDefaultCategories(savedUser);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash())) {

            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getId());

        return new LoginResponse(
                token,
                "Bearer",
                jwtExpirationMs / 1000
        );
    }
}
