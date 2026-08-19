package com.kumoh.civilai.dto.ai;

import lombok.Getter;

@Getter
public class AiDraftRequest {

    private final Long inquiryId;
    private final String question;

    public AiDraftRequest(Long inquiryId, String question) {
        this.inquiryId = inquiryId;
        this.question = question;
    }
}