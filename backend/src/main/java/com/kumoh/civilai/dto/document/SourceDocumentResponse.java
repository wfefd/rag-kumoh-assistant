package com.kumoh.civilai.dto.document;

import com.kumoh.civilai.domain.document.DocumentSource;
import com.kumoh.civilai.domain.document.SourceDocument;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SourceDocumentResponse {

    private final Long id;
    private final DocumentSource source;
    private final String sourceName;
    private final String title;
    private final String content;
    private final String url;
    private final String category;
    private final LocalDateTime collectedAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final String author;
    private final String postedDate;

    public SourceDocumentResponse(SourceDocument document) {
        this.id = document.getId();
        this.source = document.getSource();
        this.sourceName = document.getSourceName();
        this.title = document.getTitle();
        this.content = document.getContent();
        this.url = document.getUrl();
        this.category = document.getCategory();
        this.collectedAt = document.getCollectedAt();
        this.createdAt = document.getCreatedAt();
        this.updatedAt = document.getUpdatedAt();
        this.author = document.getAuthor();
        this.postedDate = document.getPostedDate();
    }
}