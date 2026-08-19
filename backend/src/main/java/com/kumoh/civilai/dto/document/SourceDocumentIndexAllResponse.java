package com.kumoh.civilai.dto.document;

import lombok.Getter;

@Getter
public class SourceDocumentIndexAllResponse {

    private final int totalCount;
    private final int successCount;
    private final int failCount;
    private final String message;

    public SourceDocumentIndexAllResponse(
            int totalCount,
            int successCount,
            int failCount,
            String message
    ) {
        this.totalCount = totalCount;
        this.successCount = successCount;
        this.failCount = failCount;
        this.message = message;
    }
}