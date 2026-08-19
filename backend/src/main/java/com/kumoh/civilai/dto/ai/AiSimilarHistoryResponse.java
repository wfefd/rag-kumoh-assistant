package com.kumoh.civilai.dto.ai;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AiSimilarHistoryResponse {

    private Long inquiryId;
    private List<SimilarHistoryItem> results;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SimilarHistoryItem {
        private Long historyId;
        private String question;
        private String answer;
        private String category;
        private Double score;
    }
}