package com.kumoh.civilai.config;

import com.kumoh.civilai.security.CustomUserDetailsService;
import com.kumoh.civilai.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()

                        // 학생 문의
                        .requestMatchers(HttpMethod.GET, "/api/inquiries/my").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.POST, "/api/inquiries").hasRole("STUDENT")

                        // 관리자 문의 관리
                        .requestMatchers(HttpMethod.GET, "/api/inquiries").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/inquiries/*/status").hasRole("ADMIN")

                        // 관리자 AI 기능
                        .requestMatchers(HttpMethod.POST, "/api/inquiries/*/ai-recommendation").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/inquiries/*/ai-recommendation").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/inquiries/*/similar-answers").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/inquiries/*/answers/approve").hasRole("ADMIN")

                        // 답변 조회는 로그인 필요, 실제 소유자 검사는 AnswerService에서 처리
                        .requestMatchers(HttpMethod.GET, "/api/inquiries/*/answers").authenticated()

                        // 문의 상세 조회는 로그인 필요, 실제 소유자 검사는 InquiryService에서 처리
                        .requestMatchers(HttpMethod.GET, "/api/inquiries/*").authenticated()

                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}