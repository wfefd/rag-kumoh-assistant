package com.kumoh.civilai.controller;

import com.kumoh.civilai.domain.document.DocumentSource;
import com.kumoh.civilai.dto.ai.AiDocumentIndexResponse;
import com.kumoh.civilai.dto.document.SourceDocumentCreateRequest;
import com.kumoh.civilai.dto.document.SourceDocumentIndexAllResponse;
import com.kumoh.civilai.dto.document.SourceDocumentResponse;
import com.kumoh.civilai.service.SourceDocumentService;
import com.kumoh.civilai.dto.document.CrawlResult;
import com.kumoh.civilai.service.SchoolNoticeCrawlerService;
import com.kumoh.civilai.service.SchoolQnaCrawlerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/source-documents")
@RequiredArgsConstructor
public class SourceDocumentController {

    private final SourceDocumentService sourceDocumentService;
    private final SchoolNoticeCrawlerService schoolNoticeCrawlerService;
    private final SchoolQnaCrawlerService schoolQnaCrawlerService;
    @PostMapping
    public SourceDocumentResponse createDocument(
            @Valid @RequestBody SourceDocumentCreateRequest request
    ) {
        return sourceDocumentService.createDocument(request);
    }

    @GetMapping
    public List<SourceDocumentResponse> getAllDocuments(
            @RequestParam(required = false) DocumentSource source,
            @RequestParam(required = false) String category
    ) {
        return sourceDocumentService.getAllDocuments(source, category);
    }

    @PostMapping("/index-all")
    public SourceDocumentIndexAllResponse indexAllDocuments() {
        return sourceDocumentService.indexAllDocuments();
    }

    @GetMapping("/{documentId}")
    public SourceDocumentResponse getDocument(@PathVariable Long documentId) {
        return sourceDocumentService.getDocument(documentId);
    }

    @PostMapping("/{documentId}/index")
    public AiDocumentIndexResponse indexDocument(@PathVariable Long documentId) {
        return sourceDocumentService.indexDocument(documentId);
    }
    @PostMapping("/crawl/notices")
    public CrawlResult crawlNotices(
            @RequestParam(defaultValue = "0") int startOffset,
            @RequestParam(defaultValue = "0") int endOffset
    ) {
        return schoolNoticeCrawlerService.crawlNotices(startOffset, endOffset);
    }
    @PostMapping("/crawl/notices/all")
    public CrawlResult crawlAllNotices() {
        return schoolNoticeCrawlerService.crawlAllNotices();
    }
    @PostMapping("/crawl/qna")
    public CrawlResult crawlQna(
            @RequestParam(defaultValue = "0") int startOffset,
            @RequestParam(defaultValue = "0") int endOffset
    ) {
        return schoolQnaCrawlerService.crawlQna(startOffset, endOffset);
    }
    @PostMapping("/crawl/qna/all")
    public CrawlResult crawlAllQna() {
        return schoolQnaCrawlerService.crawlAllQna();
    }

}