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

    /**
     * From: address for outbound mail. Separate from spring.mail.username so
     * SMTP relays whose login username is a literal (e.g. "resend") still produce
     * a valid From: header. Falls back to spring.mail.username when not set.
     */
    @Value("${app.mail.from:${spring.mail.username}}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:5173}")
    private String baseUrl;

    /**
     * Development convenience: when true, password-reset OTPs are also written
     * to the application log so they can be retrieved without a working SMTP
     * server. MUST be false in production — gated by env var.
     */
    @Value("${app.dev.log-otp:false}")
    private boolean logOtpToConsole;

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

    // ── Password reset (OTP-based) ──────────────────────────────────────────
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int OTP_MAX_ATTEMPTS = 5;
    private static final long VERIFICATION_TOKEN_EXPIRY_MS = 5 * 60 * 1000L;
    private static final String PURPOSE_PASSWORD_RESET = "password_reset";

    /**
     * Step 1 of OTP password reset.
     * Generate a fresh 6-digit OTP, invalidate any older OTPs for this user,
     * persist the SHA-256 hash with a 10-minute expiry, and email the OTP.
     * Always returns silently — never reveals whether the email exists,
     * to prevent account enumeration.
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmailAndDeletedAtIsNull(request.getEmail().toLowerCase().trim())
                .ifPresent(user -> {
                    passwordResetTokenRepository.invalidateAllForUser(user.getId());

                    String otp = generateOtp();
                    PasswordResetToken resetToken = PasswordResetToken.builder()
                            .user(user)
                            .tokenHash(hashToken(otp))
                            .expiresAt(Instant.now().plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES))
                            .attempts(0)
                            .build();
                    passwordResetTokenRepository.save(resetToken);

                    if (logOtpToConsole) {
                        log.warn("[DEV-ONLY] Password reset OTP for {}: {} (do NOT enable in production)",
                                user.getEmail(), otp);
                    }
                    sendPasswordResetOtpEmail(user, otp);
                });
    }

    /**
     * Step 2 of OTP password reset.
     * Verify the OTP and return a short-lived JWT the frontend will pass
     * back to /auth/reset-password as the verificationToken.
     */
    @Transactional
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> BusinessException.badRequest("INVALID_OTP",
                        "Invalid email or OTP"));

        PasswordResetToken token = passwordResetTokenRepository.findActiveForUser(user.getId())
                .orElseThrow(() -> BusinessException.badRequest("OTP_EXPIRED",
                        "OTP has expired or was not requested. Please request a new one."));

        if (token.getAttempts() >= OTP_MAX_ATTEMPTS) {
            // Mark used so subsequent guesses can't probe; force a fresh request.
            token.setUsedAt(Instant.now());
            passwordResetTokenRepository.save(token);
            throw BusinessException.badRequest("TOO_MANY_ATTEMPTS",
                    "Too many incorrect attempts. Please request a new OTP.");
        }

        if (!token.getTokenHash().equals(hashToken(request.getOtp()))) {
            token.setAttempts(token.getAttempts() + 1);
            passwordResetTokenRepository.save(token);
            int remaining = OTP_MAX_ATTEMPTS - token.getAttempts();
            throw BusinessException.badRequest("INVALID_OTP",
                    remaining > 0
                            ? "Incorrect OTP. " + remaining + " attempt(s) remaining."
                            : "Incorrect OTP. Please request a new one.");
        }

        // OTP is correct. Issue a single-purpose verification JWT (5 min) but
        // DO NOT mark the token as used yet — that happens in step 3 so a
        // user can't end up with a verified-but-unusable session if their
        // password set fails validation.
        String verificationToken = jwtTokenProvider.generatePurposeToken(
                user.getId(), PURPOSE_PASSWORD_RESET, VERIFICATION_TOKEN_EXPIRY_MS);

        log.info("OTP verified for user: {}", user.getEmail());
        return new VerifyOtpResponse(verificationToken, VERIFICATION_TOKEN_EXPIRY_MS / 1000);
    }

    /**
     * Step 3 of OTP password reset.
     * Validate the verification JWT, set the new password, mark the OTP used
     * and revoke all refresh tokens (forcing other devices to re-login).
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!jwtTokenProvider.isTokenValid(request.getVerificationToken())) {
            throw BusinessException.badRequest("INVALID_TOKEN",
                    "Verification token is invalid or has expired. Please restart the reset process.");
        }
        var claims = jwtTokenProvider.validateAndParseToken(request.getVerificationToken());
        if (!PURPOSE_PASSWORD_RESET.equals(claims.get("purpose", String.class))) {
            throw BusinessException.badRequest("INVALID_TOKEN", "Verification token is not valid for password reset.");
        }
        UUID userId = UUID.fromString(claims.getSubject());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.badRequest("INVALID_TOKEN", "Account not found."));

        // Defence in depth — the email submitted at step 3 must match the JWT subject.
        if (!user.getEmail().equalsIgnoreCase(request.getEmail().trim())) {
            throw BusinessException.badRequest("INVALID_TOKEN", "Verification token does not match this account.");
        }

        PasswordResetToken token = passwordResetTokenRepository.findActiveForUser(userId)
                .orElseThrow(() -> BusinessException.badRequest("OTP_EXPIRED",
                        "Reset session has expired. Please request a new OTP."));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        token.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(token);

        refreshTokenRepository.revokeAllForUser(user.getId());

        log.info("Password reset (OTP flow) completed for user: {}", user.getEmail());
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
                    .country(agent.getCountry())
                    .city(agent.getCity())
                    .address(agent.getAddress())
                    .businessType(agent.getBusinessType() != null ? agent.getBusinessType().name() : null)
                    .phone(user.getPhone())
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

    private String generateOtp() {
        // Cryptographically random 6-digit code, zero-padded so "000123" is valid.
        int code = secureRandom.nextInt(1_000_000);
        return String.format("%0" + OTP_LENGTH + "d", code);
    }

    private void sendPasswordResetOtpEmail(User user, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Your Ambiance Holidays password reset code");
            message.setText(String.format("""
                    Dear %s,

                    We received a request to reset your password. Use the one-time code below
                    on the reset form to continue:

                        %s

                    This code expires in %d minutes and can only be used once.
                    If you did not request a password reset, you can safely ignore this email
                    — your password has not been changed.

                    Best regards,
                    Ambiance Holidays Team
                    """, user.getFirstName(), otp, OTP_EXPIRY_MINUTES));
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send password reset OTP to {}: {}", user.getEmail(), e.getMessage());
        }
    }
}
