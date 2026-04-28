package com.ambianceholidays.security;

import com.ambianceholidays.domain.user.UserRole;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (StringUtils.hasText(token)) {
            // A token was sent — refuse the request outright if it's invalid/expired,
            // rather than silently treating the caller as anonymous. Otherwise an
            // expired JWT on a permitAll route (e.g. POST /bookings) drops the
            // user's identity and the booking gets stored without an owner.
            if (!jwtTokenProvider.isTokenValid(token)) {
                log.debug("Rejecting request with invalid JWT: {} {}", request.getMethod(), request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"error\":{\"code\":\"INVALID_TOKEN\",\"message\":\"Authentication token is invalid or expired\"}}");
                return;
            }

            try {
                Claims claims = jwtTokenProvider.validateAndParseToken(token);

                UUID userId = UUID.fromString(claims.getSubject());
                String email = claims.get("email", String.class);
                UserRole role = UserRole.valueOf(claims.get("role", String.class));

                String agentIdStr = claims.get("agentId", String.class);
                UUID agentId = agentIdStr != null ? UUID.fromString(agentIdStr) : null;

                SecurityPrincipal principal = new SecurityPrincipal(userId, email, role, agentId);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception e) {
                log.debug("Could not set authentication from token: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
