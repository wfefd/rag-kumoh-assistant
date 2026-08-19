package com.kumoh.civilai.dto.auth;

import com.kumoh.civilai.domain.member.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SignupRequest {

    @NotBlank
    private String loginId;

    @NotBlank
    private String password;

    @NotBlank
    private String name;

    private String studentNumber;

    @NotNull
    private Role role;
}