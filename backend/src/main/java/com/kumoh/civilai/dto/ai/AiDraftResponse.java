package com.kumoh.civilai.dto.ai;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AiDraftResponse {

    private Long inquiryId;
    private String normalizedQuestion;
    private String predictedCategory;
    private Double categoryScore;
    private List<ReferenceItem> references;
    private String draftAnswer;
    private String status;

    public String getCategory() {
        return predictedCategory;
    }

    public Double getConfidence() {
        return categoryScore;
    }

    public String getSourceSummary() {
        if (references == null || references.isEmpty()) {
            return "참고 문서 없음";
        }

        return references.stream()
                .map(ref -> ref.getSourceType() + ": " + ref.getTitle() + " (score=" + ref.getScore() + ")")
                .reduce((a, b) -> a + "\n" + b)
                .orElse("참고 문서 없음");
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ReferenceItem {
        private String sourceType;
        private Long sourceId;
        private String title;
        private Double score;
        private String content;
    }
}