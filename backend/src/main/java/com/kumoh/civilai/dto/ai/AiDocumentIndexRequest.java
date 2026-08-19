package com.kumoh.civilai.dto.ai;

import lombok.Getter;

@Getter
public class AiDocumentIndexRequest {

    private final Long documentId;
    private final String source;
    private final String sourceName;
    private final String title;
    private final String content;
    private final String url;
    private final String category;

    public AiDocumentIndexRequest(
            Long documentId,
            String source,
            String sourceName,
            String title,
            String content,
            String url,
            String category
    ) {
        this.documentId = documentId;
        this.source = source;
        this.sourceName = sourceName;
        this.title = title;
        this.content = content;
        this.url = url;
        this.category = category;
    }
}