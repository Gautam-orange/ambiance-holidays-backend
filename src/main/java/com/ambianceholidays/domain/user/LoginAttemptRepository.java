package com.ambianceholidays.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.UUID;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, UUID> {

    @Query("SELECT COUNT(a) FROM LoginAttempt a WHERE a.email = :email AND a.success = false AND a.attemptedAt >= :since")
    long countFailedSince(String email, Instant since);

    @Modifying
    @Query("DELETE FROM LoginAttempt a WHERE a.email = :email")
    void deleteAllByEmail(String email);
}
