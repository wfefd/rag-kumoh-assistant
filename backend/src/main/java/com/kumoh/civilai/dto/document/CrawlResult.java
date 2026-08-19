package com.kumoh.civilai.dto.document;

import lombok.Getter;

@Getter
public class CrawlResult {

    private final int savedCount;
    private final int skippedCount;
    private final int failedCount;
    private final String message;

    public CrawlResult(int savedCount, int skippedCount, int failedCount, String message) {
        this.savedCount = savedCount;
        this.skippedCount = skippedCount;
        this.failedCount = failedCount;
        this.message = message;
    }
}