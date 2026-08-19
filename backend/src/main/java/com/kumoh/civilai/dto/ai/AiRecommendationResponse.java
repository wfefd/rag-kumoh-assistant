package com.kumoh.civilai.dto.ai;

import com.kumoh.civilai.domain.ai.AiRecommendation;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AiRecommendationResponse {

    private final Long id;
    private final Long inquiryId;
    private final String draftAnswer;
    private final String category;
    private final Double confidence;
    private final String sourceSummary;
    private final LocalDateTime createdAt;

    public AiRecommendationResponse(AiRecommendation aiRecommendation) {
        this.id = aiRecommendation.getId();
        this.inquiryId = aiRecommendation.getInquiry().getId();
        this.draftAnswer = aiRecommendation.getDraftAnswer();
        this.category = aiRecommendation.getCategory();
        this.confidence = aiRecommendation.getConfidence();
        this.sourceSummary = aiRecommendation.getSourceSummary();
        this.createdAt = aiRecommendation.getCreatedAt();
    }
}