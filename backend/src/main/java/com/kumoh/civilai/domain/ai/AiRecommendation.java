package com.kumoh.civilai.domain.ai;

import com.kumoh.civilai.domain.inquiry.Inquiry;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 어떤 문의에 대한 AI 초안인지 연결
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false)
    private Inquiry inquiry;

    @Column(nullable = false, length = 3000)
    private String draftAnswer;

    private String category;

    private Double confidence;

    @Column(length = 1000)
    private String sourceSummary;

    private LocalDateTime createdAt;

    public AiRecommendation(
            Inquiry inquiry,
            String draftAnswer,
            String category,
            Double confidence,
            String sourceSummary
    ) {
        this.inquiry = inquiry;
        this.draftAnswer = draftAnswer;
        this.category = category;
        this.confidence = confidence;
        this.sourceSummary = sourceSummary;
        this.createdAt = LocalDateTime.now();
    }
}