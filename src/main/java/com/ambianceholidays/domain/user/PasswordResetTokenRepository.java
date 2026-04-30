package com.ambianceholidays.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /**
     * Find the most recent unused, unexpired OTP token for a user. With the
     * shift to 6-digit OTPs, multiple records can exist with the same hash
     * (one per user) so we look up by user_id, not hash.
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.user.id = :userId " +
           "AND t.usedAt IS NULL AND t.expiresAt > CURRENT_TIMESTAMP " +
           "ORDER BY t.createdAt DESC")
    Optional<PasswordResetToken> findActiveForUser(@Param("userId") UUID userId);

    /** Mark every active OTP for this user as used — called when issuing a fresh one. */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.usedAt = CURRENT_TIMESTAMP " +
           "WHERE t.user.id = :userId AND t.usedAt IS NULL")
    int invalidateAllForUser(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < CURRENT_TIMESTAMP")
    void deleteExpiredTokens();
}
