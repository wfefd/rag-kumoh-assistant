package com.kumoh.civilai.domain.answer;

import com.kumoh.civilai.domain.inquiry.Inquiry;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnswerHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 어떤 문의에 대한 최종 답변인지 연결
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false)
    private Inquiry inquiry;

    /**
     * 최종 답변 당시 사용자 질문
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    /**
     * 반복 민원 묶음을 위한 정규화된 질문
     * 예: "계절학기 시작일이 언제인가요?" -> "계절학기시작일"
     */
    @Column(length = 500)
    private String normalizedQuestion;

    /**
     * 교직원이 검토 및 승인한 최종 답변
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String finalAnswer;

    /**
     * 문의 카테고리
     */
    private String category;

    /**
     * 검토자 이름
     */
    private String reviewerName;

    /**
     * AI 서버의 ChromaDB에 HISTORY 데이터로 적재되었는지 여부
     */
    private Boolean indexed;

    private LocalDateTime createdAt;

    public AnswerHistory(
            Inquiry inquiry,
            String question,
            String finalAnswer,
            String category,
            String reviewerName
    ) {
        this.inquiry = inquiry;
        this.question = question;
        this.normalizedQuestion = normalizeQuestion(question);
        this.finalAnswer = finalAnswer;
        this.category = category;
        this.reviewerName = reviewerName;
        this.indexed = false;
        this.createdAt = LocalDateTime.now();
    }

    public void markIndexed() {
        this.indexed = true;
    }

    /**
     * 기존 데이터 보정용
     */
    public void updateNormalizedQuestion() {
        this.normalizedQuestion = normalizeQuestion(this.question);
    }

    public static String normalizeQuestion(String question) {
        if (question == null) {
            return "";
        }

        return question
                .toLowerCase()
                .replaceAll("\\s+", "")
                .replaceAll("[?!.~,:;\"'()\\[\\]{}]", "")
                .replace("알려주세요", "")
                .replace("궁금합니다", "")
                .replace("문의드립니다", "")
                .replace("문의드려요", "")
                .replace("언제인가요", "")
                .replace("언제인가요?", "")
                .replace("언제예요", "")
                .replace("언제에요", "")
                .replace("언제", "")
                .replace("인가요", "")
                .replace("되나요", "")
                .replace("되요", "")
                .replace("돼요", "")
                .replace("요", "")
                .trim();
    }
}