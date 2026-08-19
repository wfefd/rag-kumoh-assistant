package com.kumoh.civilai.service;

import com.kumoh.civilai.domain.member.Member;
import com.kumoh.civilai.domain.member.MemberRepository;
import com.kumoh.civilai.dto.auth.*;
import com.kumoh.civilai.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public MeResponse signup(SignupRequest request) {
        if (memberRepository.existsByLoginId(request.getLoginId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Member member = new Member(
                request.getLoginId(),
                encodedPassword,
                request.getName(),
                request.getStudentNumber(),
                request.getRole()
        );

        Member savedMember = memberRepository.save(member);

        return new MeResponse(savedMember);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createToken(
                member.getId(),
                member.getLoginId(),
                member.getRole()
        );

        return new LoginResponse(
                accessToken,
                member.getId(),
                member.getLoginId(),
                member.getName(),
                member.getRole()
        );
    }

    @Transactional(readOnly = true)
    public MeResponse getMe(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. id=" + memberId));

        return new MeResponse(member);
    }
}