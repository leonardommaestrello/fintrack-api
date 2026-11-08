package com.fintrack.api.auth.service.impl;

import com.fintrack.api.auth.dto.LoginRequest;
import com.fintrack.api.auth.dto.LoginResponse;
import com.fintrack.api.auth.dto.RegisterRequest;
import com.fintrack.api.auth.dto.RegisterResponse;
import com.fintrack.api.category.service.DefaultCategoryFactory;
import com.fintrack.api.common.exception.EmailAlreadyExistsException;
import com.fintrack.api.common.exception.InvalidCredentialsException;
import com.fintrack.api.security.jwt.JwtService;
import com.fintrack.api.user.entity.User;
import com.fintrack.api.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DefaultCategoryFactory defaultCategoryFactory;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "john@example.com";
    private static final String PASSWORD = "SecurePassword123!";
    private static final String PASSWORD_HASH = "encoded-password";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                authService,
                "jwtExpirationMs",
                3600000L
        );
    }

    private RegisterRequest createRegisterRequest() {
        return new RegisterRequest(
                "John",
                EMAIL,
                PASSWORD
        );
    }

    private User createUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setName("John");
        user.setEmail(EMAIL);
        user.setPasswordHash(PASSWORD_HASH);
        return user;
    }

    @Test
    void shouldRegisterUserSuccessfully() {

        RegisterRequest request = createRegisterRequest();
        User savedUser = createUser();

        when(passwordEncoder.encode(PASSWORD))
                .thenReturn(PASSWORD_HASH);

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        RegisterResponse result = authService.register(request);

        assertNotNull(result);
        assertEquals(USER_ID, result.id());
        assertEquals("John", result.name());
        assertEquals(EMAIL, result.email());

        verify(userRepository).existsByEmail(EMAIL);
        verify(passwordEncoder).encode(PASSWORD);
        verify(defaultCategoryFactory)
                .createDefaultCategories(savedUser);
    }

    @Test
    void shouldNormalizeEmailWhenRegistering() {

        RegisterRequest request = new RegisterRequest(
                "  John  ",
                "  JOHN@EXAMPLE.COM  ",
                PASSWORD
        );

        User savedUser = createUser();

        when(passwordEncoder.encode(PASSWORD))
                .thenReturn(PASSWORD_HASH);

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        authService.register(request);

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).existsByEmail(EMAIL);
        verify(userRepository).save(captor.capture());

        User userToSave = captor.getValue();

        assertEquals("John", userToSave.getName());
        assertEquals(EMAIL, userToSave.getEmail());
    }

    @Test
    void shouldRejectDuplicateEmail() {

        RegisterRequest request = createRegisterRequest();

        when(userRepository.existsByEmail(EMAIL))
                .thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> authService.register(request)
        );

        verify(userRepository, never())
                .save(any(User.class));

        verifyNoInteractions(
                passwordEncoder,
                defaultCategoryFactory
        );
    }

    @Test
    void shouldRejectPasswordExceedingBcryptLimit() {

        String longPassword = "a".repeat(73);

        RegisterRequest request = new RegisterRequest(
                "John",
                EMAIL,
                longPassword
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );

        assertEquals(
                "Password exceeds the 72-byte BCrypt limit",
                exception.getMessage()
        );

        verify(userRepository, never())
                .save(any(User.class));

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldRejectMultibytePasswordExceedingBcryptLimit() {

        String password = "á".repeat(37);

        assertTrue(
                password.getBytes(StandardCharsets.UTF_8).length > 72
        );

        RegisterRequest request = new RegisterRequest(
                "John",
                EMAIL,
                password
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldEncryptPasswordBeforeSavingUser() {

        RegisterRequest request = createRegisterRequest();

        when(passwordEncoder.encode(PASSWORD))
                .thenReturn(PASSWORD_HASH);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(USER_ID);
                    return user;
                });

        authService.register(request);

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        assertEquals(
                PASSWORD_HASH,
                captor.getValue().getPasswordHash()
        );

        assertNotEquals(
                PASSWORD,
                captor.getValue().getPasswordHash()
        );

        verify(passwordEncoder).encode(PASSWORD);
    }

    @Test
    void shouldLoginSuccessfully() {

        LoginRequest request = new LoginRequest(
                EMAIL,
                PASSWORD
        );

        User user = createUser();

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH))
                .thenReturn(true);

        when(jwtService.generateToken(USER_ID))
                .thenReturn("mock-jwt-token");

        LoginResponse result = authService.login(request);

        assertNotNull(result);
        assertEquals("mock-jwt-token", result.accessToken());
        assertEquals("Bearer", result.tokenType());
        assertEquals(3600L, result.expiresIn());

        verify(jwtService).generateToken(USER_ID);
    }

    @Test
    void shouldNormalizeEmailWhenLoggingIn() {

        LoginRequest request = new LoginRequest(
                "  JOHN@EXAMPLE.COM  ",
                PASSWORD
        );

        User user = createUser();

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH))
                .thenReturn(true);

        authService.login(request);

        verify(userRepository).findByEmail(EMAIL);
        verify(jwtService).generateToken(USER_ID);
    }

    @Test
    void shouldRejectLoginWhenUserDoesNotExist() {

        LoginRequest request = new LoginRequest(
                EMAIL,
                PASSWORD
        );

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void shouldRejectLoginWithInvalidPassword() {

        LoginRequest request = new LoginRequest(
                EMAIL,
                "WrongPassword"
        );

        User user = createUser();

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "WrongPassword",
                PASSWORD_HASH
        )).thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verify(jwtService, never())
                .generateToken(anyLong());
    }
}
