package com.ambianceholidays.api.auth.dto;

import com.ambianceholidays.domain.user.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class LoginResponse {

    private final String accessToken;
    private final String refreshToken;
    private final long expiresIn;
    private final UserInfo user;

    @Getter
    @Builder
    public static class UserInfo {
        private final UUID id;
        private final String email;
        private final String firstName;
        private final String lastName;
        private final UserRole role;
        private final AgentInfo agent;
    }

    @Getter
    @Builder
    public static class AgentInfo {
        private final UUID id;
        private final String companyName;
        private final String tier;
        private final String status;
    }
}
