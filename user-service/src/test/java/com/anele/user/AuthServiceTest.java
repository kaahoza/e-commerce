package com.anele.user;

import com.anele.user.dto.AuthResponse;
import com.anele.user.dto.LoginRequest;
import com.anele.user.exception.InvalidCredentialsException;
import com.anele.user.model.User;
import com.anele.user.repository.UserRepository;
import com.anele.user.security.JwtUtil;
import com.anele.user.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_returnsToken_whenCredentialsValid() {
        User user = User.builder().id(1L).username("jane").password("hashed").build();
        LoginRequest request = new LoginRequest();
        request.setUsername("jane");
        request.setPassword("plaintext");

        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plaintext", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken("jane", 1L)).thenReturn("fake-jwt-token");

        AuthResponse response = authService.login(request);

        assertEquals("fake-jwt-token", response.getToken());
        assertEquals("jane", response.getUsername());
    }

    @Test
    void login_throws_whenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsername("ghost");
        request.setPassword("whatever");

        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void login_throws_whenPasswordWrong() {
        User user = User.builder().id(1L).username("jane").password("hashed").build();
        LoginRequest request = new LoginRequest();
        request.setUsername("jane");
        request.setPassword("wrongpassword");

        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "hashed")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }
}
