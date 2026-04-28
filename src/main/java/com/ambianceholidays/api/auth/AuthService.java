package com.ambianceholidays.api.auth;

import com.ambianceholidays.api.auth.dto.*;
import com.ambianceholidays.domain.agent.Agent;
import com.ambianceholidays.domain.agent.AgentRepository;
import com.ambianceholidays.domain.agent.AgentStatus;
import com.ambianceholidays.domain.user.*;
import com.ambianceholidays.exception.BusinessException;
import com.ambianceholidays.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

// Maximum 5 failed attempts within 15 minutes before lockout

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_WINDOW_MINUTES = 15;

    private final UserRepository userRepository;
    private final AgentRepository agentRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JavaMailSender mailSender;

    @Value("${app.jwt.refresh-token-expiry-days}")
    private int refreshTokenExpiryDays;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:5173}")
    private String baseUrl;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress) {
        String email = request.getEmail().toLowerCase().trim();

        // Lockout check
        Instant windowStart = Instant.now().minus(LOCKOUT_WINDOW_MINUTES, ChronoUnit.MINUTES);
        long recentFailures = loginAttemptRepository.countFailedSince(email, windowStart);
        if (recentFailures >= MAX_FAILED_ATTEMPTS) {
            throw BusinessException.unauthorized("Too many failed attempts. Try again in 15 minutes.");
        }

        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseGet(() -> {
                    recordFailedAttempt(email, ipAddress);
                    throw BusinessException.unauthorized("Invalid email or password");
                });

        if (!user.isActive()) {
            throw BusinessException.unauthorized("Account is inactive");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            recordFailedAttempt(email, ipAddress);
            throw BusinessException.unauthorized("Invalid email or password");
        }

        // Clear failed attempts on successful login
        loginAttemptRepository.deleteAllByEmail(email);
        loginAttemptRepository.save(LoginAttempt.builder()
                .email(email).ipAddress(ipAddress).success(true).build());

        userRepository.updateLastLoginAt(user.getId());

        Agent agent = null;
        if (user.getRole() == UserRole.B2B_AGENT) {
            agent = agentRepository.findByUserIdAndDeletedAtIsNull(user.getId()).orElse(null);
            if (agent != null && agent.getStatus() != AgentStatus.ACTIVE) {
                String msg = agent.getStatus() == AgentStatus.PENDING
                        ? "Your account is pending admin approval. You will be notified once approved."
                        : "Your account has been suspended. Please contact support.";
                throw BusinessException.unauthorized(msg);
            }
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user,
                agent != null ? agent.getId() : null);
        String rawRefreshToken = generateSecureToken();
        String refreshTokenHash = hashToken(rawRefreshToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(refreshTokenHash)
                .expiresAt(Instant.now().plus(refreshTokenExpiryDays, ChronoUnit.DAYS))
                .build();
        refreshTokenRepository.save(refreshToken);

        return buildLoginResponse(user, agent, accessToken, rawRefreshToken);
    }

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw BusinessException.conflict("EMAIL_ALREADY_EXISTS",
                    "An account with this email already exists");
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .whatsapp(request.getWhatsapp())
                .role(UserRole.B2B_AGENT)
                .active(true)
                .emailVerified(false)
                .build();
        userRepository.save(user);

        Agent agent = Agent.builder()
                .user(user)
                .companyName(request.getCompanyName())
                .country(request.getCountry())
                .city(request.getCity())
                .address(request.getAddress())
                .businessType(request.getBusinessType())
                .build();
        agentRepository.save(agent);

        sendVerificationEmail(user);
        sendRegistrationEmail(user, agent);
        log.info("New agent registered: {} ({})", user.getEmail(), agent.getCompanyName());
    }

    @Transactional
    public LoginResponse refresh(RefreshTokenRequest request) {
        String tokenHash = hashToken(request.getRefreshToken());
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> BusinessException.unauthorized("Invalid or expired refresh token"));

        if (!refreshToken.isValid()) {
            // Possible token theft — revoke all tokens for this user
            refreshTokenRepository.revokeAllForUser(refreshToken.getUser().getId());
            throw BusinessException.unauthorized("Refresh token compromised. Please log in again.");
        }

        // Rotate: revoke old token
        refreshToken.setRevokedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);

        User user = refreshToken.getUser();
        Agent agent = user.getRole() == UserRole.B2B_AGENT
                ? agentRepository.findByUserIdAndDeletedAtIsNull(user.getId()).orElse(null)
                : null;

        String accessToken = jwtTokenProvider.generateAccessToken(user,
                agent != null ? agent.getId() : null);
        String newRawRefreshToken = generateSecureToken();
        String newTokenHash = hashToken(newRawRefreshToken);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(newTokenHash)
                .expiresAt(Instant.now().plus(refreshTokenExpiryDays, ChronoUnit.DAYS))
                .build();
        refreshTokenRepository.save(newRefreshToken);

        return buildLoginResponse(user, agent, accessToken, newRawRefreshToken);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        String tokenHash = hashToken(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.setRevokedAt(Instant.now());
            refreshTokenRepository.save(token);
        });
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Always return success to prevent email enumeration
        userRepository.findByEmailAndDeletedAtIsNull(request.getEmail()).ifPresent(user -> {
            String rawToken = generateSecureToken();
            String tokenHash = hashToken(rawToken);

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .tokenHash(tokenHash)
                    .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                    .build();
            passwordResetTokenRepository.save(resetToken);

            sendPasswordResetEmail(user, rawToken);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String tokenHash = hashToken(request.getToken());
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> BusinessException.badRequest("INVALID_TOKEN",
                        "Invalid or expired reset token"));

        if (!resetToken.isValid()) {
            throw BusinessException.badRequest("INVALID_TOKEN", "Reset token has expired or already been used");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(resetToken);

        // Revoke all refresh tokens for security
        refreshTokenRepository.revokeAllForUser(user.getId());

        log.info("Password reset for user: {}", user.getEmail());
    }

    @Transactional
    public void verifyEmail(String rawToken) {
        String tokenHash = hashToken(rawToken);
        User user = userRepository.findByVerificationTokenHash(tokenHash)
                .orElseThrow(() -> BusinessException.badRequest("INVALID_TOKEN", "Invalid or expired verification token"));

        if (user.getVerificationTokenExpiresAt() == null || Instant.now().isAfter(user.getVerificationTokenExpiresAt())) {
            throw BusinessException.badRequest("TOKEN_EXPIRED", "Verification link has expired. Request a new one.");
        }

        user.setEmailVerified(true);
        user.setVerificationTokenHash(null);
        user.setVerificationTokenExpiresAt(null);
        userRepository.save(user);
        log.info("Email verified for user: {}", user.getEmail());
    }

    @Transactional
    public void resendVerification(String email) {
        // Always return success to prevent email enumeration
        userRepository.findByEmailAndDeletedAtIsNull(email.toLowerCase().trim()).ifPresent(user -> {
            if (!user.isEmailVerified()) {
                sendVerificationEmail(user);
            }
        });
    }

    private void recordFailedAttempt(String email, String ipAddress) {
        loginAttemptRepository.save(LoginAttempt.builder()
                .email(email).ipAddress(ipAddress).success(false).build());
    }

    public LoginResponse.UserInfo getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("User"));

        Agent agent = user.getRole() == UserRole.B2B_AGENT
                ? agentRepository.findByUserIdAndDeletedAtIsNull(userId).orElse(null)
                : null;

        return buildUserInfo(user, agent);
    }

    private LoginResponse buildLoginResponse(User user, Agent agent, String accessToken, String rawRefreshToken) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .expiresIn(900)
                .user(buildUserInfo(user, agent))
                .build();
    }

    private LoginResponse.UserInfo buildUserInfo(User user, Agent agent) {
        LoginResponse.AgentInfo agentInfo = null;
        if (agent != null) {
            agentInfo = LoginResponse.AgentInfo.builder()
                    .id(agent.getId())
                    .companyName(agent.getCompanyName())
                    .tier(agent.getTier().name())
                    .status(agent.getStatus().name())
                    .build();
        }

        return LoginResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .agent(agentInfo)
                .build();
    }

    private String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private void sendVerificationEmail(User user) {
        String rawToken = generateSecureToken();
        String tokenHash = hashToken(rawToken);
        user.setVerificationTokenHash(tokenHash);
        user.setVerificationTokenExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
        userRepository.save(user);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Verify your email — Ambiance Holidays");
            message.setText(String.format("""
                    Dear %s,

                    Please verify your email address by clicking the link below (valid for 24 hours):

                    %s/verify-email/%s

                    If you did not create an account, please ignore this email.

                    Best regards,
                    Ambiance Holidays Team
                    """, user.getFirstName(), baseUrl, rawToken));
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send verification email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private void sendRegistrationEmail(User user, Agent agent) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Welcome to Ambiance Holidays — Registration Received");
            message.setText(String.format("""
                    Dear %s,

                    Thank you for registering as a B2B partner with Ambiance Holidays.

                    Your account for %s is currently pending admin review.
                    You will receive another email once your account has been approved.

                    Best regards,
                    Ambiance Holidays Team
                    """, user.getFirstName(), agent.getCompanyName()));
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send registration email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private void sendPasswordResetEmail(User user, String rawToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Reset your Ambiance Holidays password");
            message.setText(String.format("""
                    Dear %s,

                    We received a request to reset your password. Use the token below in the reset form:

                    Token: %s

                    This token expires in 1 hour. If you did not request this, please ignore this email.

                    Best regards,
                    Ambiance Holidays Team
                    """, user.getFirstName(), rawToken));
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send password reset email to {}: {}", user.getEmail(), e.getMessage());
        }
    }
}
