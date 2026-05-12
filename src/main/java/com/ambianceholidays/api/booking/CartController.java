package com.ambianceholidays.api.booking;

import com.ambianceholidays.api.booking.dto.*;
import com.ambianceholidays.common.dto.ApiResponse;
import com.ambianceholidays.domain.agent.AgentRepository;
import com.ambianceholidays.security.SecurityPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final AgentRepository agentRepo;

    public CartController(CartService cartService, AgentRepository agentRepo) {
        this.cartService = cartService;
        this.agentRepo = agentRepo;
    }

    /**
     * Session key = "user:{userId}". The platform is agent-only, so we no
     * longer accept anonymous / guest carts — a missing principal here means
     * the caller is unauthenticated and must log in.
     */
    private String sessionKey(@RequestHeader(value = "X-Cart-Id", required = false) String cartId,
                              SecurityPrincipal principal) {
        if (principal == null) {
            throw com.ambianceholidays.exception.BusinessException.unauthorized(
                    "Sign in to manage your cart.");
        }
        return "user:" + principal.getUserId();
    }

    @GetMapping
    public ApiResponse<CartSummaryResponse> getCart(
            @RequestHeader(value = "X-Cart-Id", required = false) String cartId,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        BigDecimal agentMarkup = resolveAgentMarkup(principal);
        return cartService.getCart(sessionKey(cartId, principal), agentMarkup);
    }

    @PostMapping("/items")
    public ApiResponse<CartSummaryResponse> addItem(
            @RequestHeader(value = "X-Cart-Id", required = false) String cartId,
            @AuthenticationPrincipal SecurityPrincipal principal,
            @Valid @RequestBody AddCartItemRequest req) {
        return cartService.addItem(sessionKey(cartId, principal), req);
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<CartSummaryResponse> removeItem(
            @RequestHeader(value = "X-Cart-Id", required = false) String cartId,
            @AuthenticationPrincipal SecurityPrincipal principal,
            @PathVariable UUID id) {
        return cartService.removeItem(sessionKey(cartId, principal), id);
    }

    @DeleteMapping
    public ApiResponse<Void> clearCart(
            @RequestHeader(value = "X-Cart-Id", required = false) String cartId,
            @AuthenticationPrincipal SecurityPrincipal principal) {
        return cartService.clearCart(sessionKey(cartId, principal));
    }

    private BigDecimal resolveAgentMarkup(SecurityPrincipal principal) {
        if (principal == null) return BigDecimal.ZERO;
        return agentRepo.findByUserIdAndDeletedAtIsNull(principal.getUserId())
                .map(a -> a.getMarkupPercent())
                .orElse(BigDecimal.ZERO);
    }
}
