package com.indianroadmap.user.service;

import com.indianroadmap.user.document.AccountStatus;
import com.indianroadmap.user.document.UserDocument;
import com.indianroadmap.user.document.UserPreferences;
import com.indianroadmap.user.document.UserRole;
import com.indianroadmap.user.dto.request.LoginRequest;
import com.indianroadmap.user.dto.request.LogoutRequest;
import com.indianroadmap.user.dto.request.RefreshTokenRequest;
import com.indianroadmap.user.dto.request.RegisterRequest;
import com.indianroadmap.user.dto.response.AuthResponse;
import com.indianroadmap.user.dto.response.UserResponse;
import com.indianroadmap.user.exception.AccountDisabledException;
import com.indianroadmap.user.exception.AccountLockedException;
import com.indianroadmap.user.exception.InvalidCredentialsException;
import com.indianroadmap.user.exception.RefreshTokenInvalidException;
import com.indianroadmap.user.exception.UserAlreadyExistsException;
import com.indianroadmap.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final Clock clock;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordService passwordService,
                           JwtService jwtService,
                           RefreshTokenService refreshTokenService,
                           Clock clock) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.clock = clock;
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException(normalizedEmail);
        }

        Instant now = Instant.now(clock);
        UserDocument document = new UserDocument();
        document.setEmail(normalizedEmail);
        document.setPasswordHash(passwordService.hashPassword(request.password()));
        document.setFirstName(request.firstName().trim());
        document.setLastName(request.lastName().trim());
        document.setDisplayName(resolveDisplayName(request.displayName(), request.firstName(), request.lastName()));
        document.setRole(UserRole.USER);
        document.setStatus(AccountStatus.ACTIVE);
        document.setPreferences(new UserPreferences());
        document.setCreatedAt(now);
        document.setUpdatedAt(now);

        return UserResponse.from(userRepository.save(document));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        UserDocument user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordService.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        ensureActiveForAuthentication(user);

        Instant now = Instant.now(clock);
        user.setLastLoginAt(now);
        user.setUpdatedAt(now);
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = refreshTokenService.createRefreshToken(user.getId());
        return AuthResponse.of(accessToken, refreshToken, jwtService.getAccessTokenExpirationSeconds());
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        var existingToken = refreshTokenService.requireValidToken(request.refreshToken());
        UserDocument user = userRepository.findById(existingToken.getUserId())
                .orElseThrow(RefreshTokenInvalidException::new);
        ensureActiveForAuthentication(user);

        String newRefreshToken = refreshTokenService.rotateToken(existingToken);
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole());
        return AuthResponse.of(accessToken, newRefreshToken, jwtService.getAccessTokenExpirationSeconds());
    }

    @Override
    public void logout(LogoutRequest request) {
        refreshTokenService.revokeToken(request.refreshToken());
    }

    private void ensureActiveForAuthentication(UserDocument user) {
        if (user.getStatus() == AccountStatus.LOCKED) {
            throw new AccountLockedException("Account is locked");
        }
        if (user.getStatus() == AccountStatus.DISABLED || user.getStatus() == AccountStatus.PENDING_VERIFICATION) {
            throw new AccountDisabledException("Account is not active");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String resolveDisplayName(String displayName, String firstName, String lastName) {
        if (displayName != null && !displayName.isBlank()) {
            return displayName.trim();
        }
        return firstName.trim() + " " + lastName.trim();
    }
}
