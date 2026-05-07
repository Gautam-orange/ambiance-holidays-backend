package com.ambianceholidays.api.auth;

import com.ambianceholidays.api.auth.dto.LoginResponse;
import com.ambianceholidays.common.dto.ApiResponse;
import com.ambianceholidays.security.SecurityPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MeController {

    private final AuthService authService;

    /**
     * Mounted at both `/auth/me` (the canonical path the SPA uses) and `/me`
     * (what the controller used to expose) so existing integrations keep
     * working. Either lands on the same handler.
     */
    @GetMapping({"/auth/me", "/me"})
    public ResponseEntity<ApiResponse<LoginResponse.UserInfo>> me(
            @AuthenticationPrincipal SecurityPrincipal principal) {
        LoginResponse.UserInfo user = authService.getCurrentUser(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(user));
    }
}
