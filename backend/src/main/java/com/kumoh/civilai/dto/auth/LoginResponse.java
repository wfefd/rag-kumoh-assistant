package com.kumoh.civilai.dto.auth;

import com.kumoh.civilai.domain.member.Role;
import lombok.Getter;

@Getter
public class LoginResponse {

    private final String accessToken;
    private final Long memberId;
    private final String loginId;
    private final String name;
    private final Role role;

    public LoginResponse(String accessToken, Long memberId, String loginId, String name, Role role) {
        this.accessToken = accessToken;
        this.memberId = memberId;
        this.loginId = loginId;
        this.name = name;
        this.role = role;
    }
}