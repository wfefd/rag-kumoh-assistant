package com.kumoh.civilai.dto.answer;

import com.kumoh.civilai.domain.answer.AnswerHistory;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AnswerHistoryResponse {

    private final Long id;
    private final Long inquiryId;
    private final String question;
    private final String finalAnswer;
    private final String category;
    private final String reviewerName;
    private final Boolean indexed;
    private final LocalDateTime createdAt;

    public AnswerHistoryResponse(AnswerHistory answerHistory) {
        this.id = answerHistory.getId();
        this.inquiryId = answerHistory.getInquiry().getId();
        this.question = answerHistory.getQuestion();
        this.finalAnswer = answerHistory.getFinalAnswer();
        this.category = answerHistory.getCategory();
        this.reviewerName = answerHistory.getReviewerName();
        this.indexed = answerHistory.getIndexed();
        this.createdAt = answerHistory.getCreatedAt();
    }
}