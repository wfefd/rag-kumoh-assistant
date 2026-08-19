package com.kumoh.civilai.security;

import com.kumoh.civilai.domain.member.Member;
import com.kumoh.civilai.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String memberId) throws UsernameNotFoundException {
        Member member = memberRepository.findById(Long.parseLong(memberId))
                .orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없습니다. id=" + memberId));

        return new User(
                String.valueOf(member.getId()),
                member.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()))
        );
    }
}