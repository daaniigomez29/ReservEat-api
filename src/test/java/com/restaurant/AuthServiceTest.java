package com.restaurant;

import com.restaurant.application.dto.response.AuthResponse;
import com.restaurant.application.port.in.EmailVerificationUseCase;
import com.restaurant.application.usecase.AuthService;
import com.restaurant.domain.model.AuthUser;
import com.restaurant.domain.model.GlobalRole;
import com.restaurant.domain.model.RefreshToken;
import com.restaurant.domain.repository.AuthUserRepository;
import com.restaurant.domain.repository.RefreshTokenRepository;
import com.restaurant.domain.valueobject.UserId;
import com.restaurant.infrastructure.security.RefreshTokenHasher;
import com.restaurant.infrastructure.security.google.GoogleIdTokenService;
import com.restaurant.infrastructure.security.jwt.JwtService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthUserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private GoogleIdTokenService googleIdTokenService;
    @Mock private EmailVerificationUseCase emailVerificationUseCase;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private RefreshTokenHasher refreshTokenHasher;

    @InjectMocks
    private AuthService authService;

    private AuthUser user() {
        return AuthUser.builder()
                .id(new UserId(1L))
                .email("user@test.com")
                .username("user1")
                .globalRole(GlobalRole.USER)
                .emailVerified(true)
                .build();
    }

    @Test
    @DisplayName("Logout deletes the session row for the presented refresh token")
    void logout_invalidatesTokenByHash() {
        when(refreshTokenHasher.hash("raw-refresh")).thenReturn("hashed");

        authService.logout("raw-refresh");

        verify(refreshTokenRepository).deleteByTokenHash("hashed");
    }

    @Test
    @DisplayName("Refresh is rejected when the session was revoked, even if the JWT is still valid")
    void refreshToken_whenSessionRevoked_throwsAndIssuesNothing() {
        AuthUser user = user();
        when(jwtService.extractUsername("raw")).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("raw", user)).thenReturn(true);
        when(refreshTokenHasher.hash("raw")).thenReturn("hashed");
        when(refreshTokenRepository.findByTokenHash("hashed")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken("raw"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no longer valid");

        verify(refreshTokenRepository, never()).deleteByTokenHash(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Refresh rotates the token: old session deleted, a new one stored")
    void refreshToken_whenValid_rotatesAndIssuesNewPair() {
        AuthUser user = user();
        when(jwtService.extractUsername("old-refresh")).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("old-refresh", user)).thenReturn(true);
        when(refreshTokenHasher.hash("old-refresh")).thenReturn("old-hash");
        when(refreshTokenRepository.findByTokenHash("old-hash"))
                .thenReturn(Optional.of(RefreshToken.builder().tokenHash("old-hash").build()));

        // issueTokensFor(...)
        when(jwtService.generateToken(user)).thenReturn("new-access");
        when(jwtService.generateRefreshToken(user)).thenReturn("new-refresh");
        when(refreshTokenHasher.hash("new-refresh")).thenReturn("new-hash");
        when(jwtService.getExpiration("new-refresh")).thenReturn(LocalDateTime.now().plusDays(7));

        AuthResponse response = authService.refreshToken("old-refresh");

        // old session consumed
        verify(refreshTokenRepository).deleteByTokenHash("old-hash");

        // new session persisted (hash only)
        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(saved.capture());
        assertThat(saved.getValue().getTokenHash()).isEqualTo("new-hash");
        assertThat(saved.getValue().getUserId()).isEqualTo(1L);

        // fresh pair returned to the client
        assertThat(response.getToken()).isEqualTo("new-access");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh");
    }
}
