package com.kumoh.civilai.controller;

import com.kumoh.civilai.dto.auth.*;
import com.kumoh.civilai.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public MeResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public void logout() {
        // JWT 방식에서는 서버가 상태를 저장하지 않으므로
        // 기본 로그아웃은 프론트에서 토큰 삭제로 처리한다.
    }

    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        return authService.getMe(memberId);
    }
}