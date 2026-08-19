package com.kumoh.civilai.domain.member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(length = 30)
    private String studentNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Member(String loginId, String password, String name, String studentNumber, Role role) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.studentNumber = studentNumber;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }
}