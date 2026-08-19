package com.kumoh.civilai.dto.ai;

public record AiSimilarHistoryRequest(
        Long inquiryId,
        String question,
        Integer topK
) {
}