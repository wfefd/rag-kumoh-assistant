package com.kumoh.civilai.domain.inquiry;

import com.kumoh.civilai.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인한 회원과 문의 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String studentName;

    private String studentNumber;

    @Column(nullable = false, length = 1000)
    private String content;

    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Inquiry(Member member, String content) {
        this.member = member;
        this.studentName = member.getName();
        this.studentNumber = member.getStudentNumber();
        this.content = content;
        this.status = InquiryStatus.RECEIVED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 기존 데이터/테스트용 생성자는 당장은 남겨둬도 됨
    public Inquiry(String studentName, String studentNumber, String content) {
        this.studentName = studentName;
        this.studentNumber = studentNumber;
        this.content = content;
        this.status = InquiryStatus.RECEIVED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus(InquiryStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateCategory(String category) {
        this.category = category;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOwner(Long memberId) {
        return this.member != null && this.member.getId().equals(memberId);
    }
}