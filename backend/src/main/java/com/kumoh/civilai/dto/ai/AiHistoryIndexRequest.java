package com.kumoh.civilai.dto.ai;

import lombok.Getter;

@Getter
public class AiHistoryIndexRequest {

    private final Long historyId;
    private final Long inquiryId;
    private final String source;
    private final String title;
    private final String content;
    private final String category;

    public AiHistoryIndexRequest(
            Long historyId,
            Long inquiryId,
            String source,
            String title,
            String content,
            String category
    ) {
        this.historyId = historyId;
        this.inquiryId = inquiryId;
        this.source = source;
        this.title = title;
        this.content = content;
        this.category = category;
    }
}