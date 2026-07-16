package com.restaurant.application.usecase;

import com.restaurant.application.dto.request.GoogleLoginRequest;
import com.restaurant.application.dto.request.LoginRequest;
import com.restaurant.application.dto.request.RegisterRequest;
import com.restaurant.application.dto.response.AuthResponse;
import com.restaurant.application.dto.response.RegistrationResponse;
import com.restaurant.application.port.in.AuthUseCase;
import com.restaurant.application.port.in.EmailVerificationUseCase;
import com.restaurant.domain.exception.EmailAlreadyExistsException;
import com.restaurant.domain.exception.EmailNotVerifiedException;
import com.restaurant.domain.exception.UserNotFoundException;
import com.restaurant.domain.exception.UsernameAlreadyExistsException;
import com.restaurant.domain.model.AuthUser;
import com.restaurant.domain.model.GlobalRole;
import com.restaurant.domain.model.RefreshToken;
import com.restaurant.domain.repository.AuthUserRepository;
import com.restaurant.domain.repository.RefreshTokenRepository;
import com.restaurant.infrastructure.security.RefreshTokenHasher;
import com.restaurant.infrastructure.security.google.GoogleIdTokenClaims;
import com.restaurant.infrastructure.security.google.GoogleIdTokenService;
import com.restaurant.infrastructure.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final AuthUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final GoogleIdTokenService googleIdTokenService;
    private final EmailVerificationUseCase emailVerificationUseCase;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenHasher refreshTokenHasher;

    @Override
    @Transactional
    public RegistrationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException(request.getUsername());
        }

        AuthUser user = AuthUser.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .name(request.getName())
                .tlf(request.getTlf())
                .password(passwordEncoder.encode(request.getPassword()))
                .globalRole(GlobalRole.USER)
                .emailVerified(false)
                .build();

        try {
            AuthUser saved = userRepository.save(user);
            log.info("New user registered (pending verification): {}", saved.getEmail());

            // No auto-login: the account stays unverified and login is blocked until
            // the user clicks the link in the verification email.
            emailVerificationUseCase.startVerification(saved);

            return RegistrationResponse.builder()
                    .userId(saved.getId().value())
                    .email(saved.getEmail())
                    .emailVerified(false)
                    .message("Registro completado. Revisa tu correo para verificar tu cuenta.")
                    .build();
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException(user.getEmail());
        }
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        AuthUser user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));

        // Credentials are valid at this point; block access until the email is verified.
        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException(user.getEmail());
        }

        log.info("User logged in: {}", user.getEmail());
        return issueTokensFor(user);
    }

    @Override
    @Transactional
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        GoogleIdTokenClaims claims = googleIdTokenService.verifyAndExtract(request.getIdToken());
        if (!claims.emailVerified()) {
            throw new IllegalArgumentException("Google account email is not verified");
        }

        AuthUser user = userRepository.findByEmail(claims.email())
                .orElseGet(() -> {
                    String username = generateUniqueUsernameFromEmail(claims.email());
                    String name = (claims.name() != null && !claims.name().isBlank()) ? claims.name() : username;

                    AuthUser newUser = AuthUser.builder()
                            .email(claims.email())
                            .username(username)
                            .name(name)
                            .tlf(null)
                            .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                            .globalRole(GlobalRole.USER)
                            // Google already proved ownership of this email, so skip verification.
                            .emailVerified(true)
                            .build();

                    AuthUser saved = userRepository.save(newUser);
                    log.info("New user registered via Google: {}", saved.getEmail());
                    return saved;
                });

        log.info("User logged in with Google: {}", user.getEmail());
        return issueTokensFor(user);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtService.extractUsername(refreshToken);
        AuthUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // Server-side revocation check: the JWT may still be signed and unexpired,
        // but if its session row is gone (logged out, or already rotated away) the
        // token is no longer accepted.
        String tokenHash = refreshTokenHasher.hash(refreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token is no longer valid"));

        // Rotation: consume the presented token so it cannot be replayed, then
        // issue a fresh pair.
        refreshTokenRepository.deleteByTokenHash(tokenHash);

        log.info("Tokens refreshed for: {}", user.getEmail());
        return issueTokensFor(user);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        // Deleting a missing hash is a no-op, so logout is idempotent and never
        // fails even if the client's token was already rotated or expired.
        refreshTokenRepository.deleteByTokenHash(refreshTokenHasher.hash(refreshToken));
        log.info("Refresh token invalidated on logout");
    }

    /**
     * Issues a fresh access + refresh token pair and records the refresh token as
     * an active session (storing only its hash). Shared by login, Google login
     * and refresh rotation.
     */
    private AuthResponse issueTokensFor(AuthUser user) {
        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        refreshTokenRepository.save(RefreshToken.builder()
                .tokenHash(refreshTokenHasher.hash(refreshToken))
                .userId(user.getId().value())
                .expiresAt(jwtService.getExpiration(refreshToken))
                .createdAt(LocalDateTime.now())
                .build());

        return buildAuthResponse(token, refreshToken, user);
    }

    private AuthResponse buildAuthResponse(String token, String refreshToken, AuthUser user) {
        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId().value())
                .email(user.getEmail())
                .username(user.getUsername())
                .globalRole(user.getGlobalRole())
                .restaurantRole(user.getRestaurantRole())
                .build();
    }

    private String generateUniqueUsernameFromEmail(String email) {
        String localPart = email.split("@", 2)[0];
        String base = localPart.replaceAll("[^a-zA-Z0-9._-]", "");
        if (base.isBlank()) {
            base = "user";
        }

        String candidate = base;
        int i = 0;
        while (userRepository.existsByUsername(candidate)) {
            i++;
            candidate = base + i;
        }
        return candidate;
    }
}
