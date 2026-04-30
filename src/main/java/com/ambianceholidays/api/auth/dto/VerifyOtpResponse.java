package com.ambianceholidays.api.auth.dto;

/**
 * Returned by /auth/verify-otp once the user has entered the correct OTP.
 * The frontend stores this short-lived token and passes it back to
 * /auth/reset-password along with the new password.
 *
 * @param verificationToken JWT signed with the same key as access tokens; carries
 *                          purpose=password_reset and expires after expiresInSeconds.
 * @param expiresInSeconds  How long the token is valid (typically 300 / 5 min).
 */
public record VerifyOtpResponse(String verificationToken, long expiresInSeconds) {}
