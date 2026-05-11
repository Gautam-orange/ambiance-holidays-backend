package com.ambianceholidays.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByVerificationTokenHash(String verificationTokenHash);

    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    void updateLastLoginAt(UUID id);

    /** Active users in the given roles. Used by the notification helper to
     *  fetch SUPER_ADMIN + ADMIN_OPS recipients. Native query because the
     *  `user_role` postgres enum trips up the JPQL enum cast otherwise. */
    @Query(value = "SELECT * FROM users "
            + "WHERE deleted_at IS NULL AND is_active = true "
            + "AND role::text IN ('SUPER_ADMIN','ADMIN_OPS')",
            nativeQuery = true)
    List<User> findActiveAdmins();
}
