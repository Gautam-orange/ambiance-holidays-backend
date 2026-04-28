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
@RequestMapping("/me")
@RequiredArgsConstructor
public class MeController {

    private final AuthService authService;

    @GetMapping
    public ResponseEntity<ApiResponse<LoginResponse.UserInfo>> me(
            @AuthenticationPrincipal SecurityPrincipal principal) {
        LoginResponse.UserInfo user = authService.getCurrentUser(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(user));
    }
}
