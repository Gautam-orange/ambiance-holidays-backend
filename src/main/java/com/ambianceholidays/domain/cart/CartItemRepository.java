package com.ambianceholidays.domain.cart;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    List<CartItem> findBySessionKeyAndExpiresAtAfter(String sessionKey, Instant now);
    void deleteBySessionKey(String sessionKey);
    void deleteByExpiresAtBefore(Instant now);
}
