package com.mib.backend.service.impl;

import com.mib.backend.dto.request.LoginRequest;
import com.mib.backend.dto.request.RefreshTokenRequest;
import com.mib.backend.dto.request.RegisterRequest;
import com.mib.backend.dto.response.AuthResponse;
import com.mib.backend.entity.Profile;
import com.mib.backend.entity.RefreshToken;
import com.mib.backend.entity.Role;
import com.mib.backend.entity.RoleName;
import com.mib.backend.entity.User;
import com.mib.backend.entity.XpReasonType;
import com.mib.backend.exception.AccountSuspendedException;
import com.mib.backend.exception.EmailAlreadyInUseException;
import com.mib.backend.exception.InvalidCredentialsException;
import com.mib.backend.exception.InvalidRefreshTokenException;
import com.mib.backend.exception.UsernameAlreadyInUseException;
import com.mib.backend.mapper.UserMapper;
import com.mib.backend.repository.RefreshTokenRepository;
import com.mib.backend.repository.RoleRepository;
import com.mib.backend.repository.UserRepository;
import com.mib.backend.security.JwtService;
import com.mib.backend.service.AchievementCodes;
import com.mib.backend.service.AchievementService;
import com.mib.backend.service.AuthService;
import com.mib.backend.service.XpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final XpService xpService;
    private final AchievementService achievementService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new EmailAlreadyInUseException("Este email ja esta em uso");
        }
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new UsernameAlreadyInUseException("Este nome de usuario ja esta em uso");
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_USER)));

        User user = new User(request.username(), request.email(), passwordEncoder.encode(request.password()));
        user.getRoles().add(userRole);

        Profile profile = new Profile(user, request.displayName());
        user.setProfile(profile);

        user.setCurrentLoginStreak(1);
        user.setLongestLoginStreak(1);

        User saved = userRepository.save(user);

        achievementService.grant(saved, AchievementCodes.FIRST_LOGIN);

        return issueTokens(saved);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.emailOrUsername())
                .or(() -> userRepository.findByUsernameIgnoreCase(request.emailOrUsername()))
                .orElseThrow(() -> new InvalidCredentialsException("Email/usuario ou senha invalidos"));

        if (user.isBanned()) {
            throw new AccountSuspendedException("Esta conta foi banida");
        }
        if (user.isCurrentlySuspended()) {
            throw new AccountSuspendedException("Esta conta esta suspensa temporariamente ate " + user.getSuspendedUntil());
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Email/usuario ou senha invalidos");
        }

        processDailyLogin(user);

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        return issueTokens(user);
    }

    /**
     * No primeiro acesso do dia: credita XP diario, atualiza o streak de dias
     * consecutivos (reinicia se houve uma lacuna maior que 1 dia) e verifica as
     * conquistas de streak (7 e 30 dias). Acessar de novo no mesmo dia nao repete nada.
     */
    private void processDailyLogin(User user) {
        Instant previousLogin = user.getLastLoginAt();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate previousLoginDate = previousLogin != null ? LocalDate.ofInstant(previousLogin, ZoneOffset.UTC) : null;

        boolean alreadyLoggedInToday = today.equals(previousLoginDate);
        if (alreadyLoggedInToday) {
            return;
        }

        xpService.awardXp(user, 10, XpReasonType.DAILY_LOGIN, "Acesso diario ao aplicativo");

        boolean consecutiveDay = today.minusDays(1).equals(previousLoginDate);
        int newStreak = consecutiveDay ? user.getCurrentLoginStreak() + 1 : 1;
        user.setCurrentLoginStreak(newStreak);
        if (newStreak > user.getLongestLoginStreak()) {
            user.setLongestLoginStreak(newStreak);
        }

        achievementService.checkLoginStreak(user, newStreak);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token invalido"));

        if (stored.isRevoked() || stored.isExpired()) {
            throw new InvalidRefreshTokenException("Refresh token expirado ou revogado. Faca login novamente");
        }

        User user = stored.getUser();
        if (user.isBanned() || user.isCurrentlySuspended()) {
            throw new AccountSuspendedException("Esta conta nao pode gerar novos tokens no momento");
        }

        // Rotaciona o refresh token: revoga o antigo e emite um novo par de tokens.
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return issueTokens(user);
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByToken(request.refreshToken())
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername());
        String refreshTokenValue = jwtService.generateOpaqueRefreshToken();

        Instant expiresAt = Instant.now().plus(jwtService.getRefreshTokenExpirationMs(), ChronoUnit.MILLIS);
        refreshTokenRepository.save(new RefreshToken(user, refreshTokenValue, expiresAt));

        return AuthResponse.of(accessToken, refreshTokenValue, userMapper.toUserSummary(user));
    }
}
