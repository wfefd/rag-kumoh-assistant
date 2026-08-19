package com.kumoh.civilai.dto.admin;

import lombok.Getter;

@Getter
public class ReusableAnswerResponse {

    private final Long id;
    private final String question;
    private final String answer;
    private final String category;
    private final Long usedCount;

    public ReusableAnswerResponse(
            Long id,
            String question,
            String answer,
            String category,
            Long usedCount
    ) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.category = category;
        this.usedCount = usedCount;
    }
}