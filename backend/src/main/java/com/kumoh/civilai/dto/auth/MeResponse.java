package com.kumoh.civilai.dto.auth;

import com.kumoh.civilai.domain.member.Member;
import com.kumoh.civilai.domain.member.Role;
import lombok.Getter;

@Getter
public class MeResponse {

    private final Long memberId;
    private final String loginId;
    private final String name;
    private final String studentNumber;
    private final Role role;

    public MeResponse(Member member) {
        this.memberId = member.getId();
        this.loginId = member.getLoginId();
        this.name = member.getName();
        this.studentNumber = member.getStudentNumber();
        this.role = member.getRole();
    }
}