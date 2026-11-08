package com.fintrack.api.user.controller;

import com.fintrack.api.user.entity.User;
import com.fintrack.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow();

        return ResponseEntity.ok(
                new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail()
                )
        );
    }

    public record UserResponse(
            Long id,
            String name,
            String email
    ) {}
}
