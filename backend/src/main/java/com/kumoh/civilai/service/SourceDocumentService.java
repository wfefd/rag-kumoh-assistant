package com.kumoh.civilai.service;

import com.kumoh.civilai.domain.document.DocumentSource;
import com.kumoh.civilai.domain.document.SourceDocument;
import com.kumoh.civilai.domain.document.SourceDocumentRepository;
import com.kumoh.civilai.dto.ai.AiDocumentIndexRequest;
import com.kumoh.civilai.dto.ai.AiDocumentIndexResponse;
import com.kumoh.civilai.dto.document.SourceDocumentCreateRequest;
import com.kumoh.civilai.dto.document.SourceDocumentResponse;
import com.kumoh.civilai.dto.document.SourceDocumentIndexAllResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SourceDocumentService {

    private final SourceDocumentRepository sourceDocumentRepository;
    private final AiServerClient aiServerClient;

    @Transactional
    public SourceDocumentResponse createDocument(SourceDocumentCreateRequest request) {
        if (request.getUrl() != null && !request.getUrl().isBlank()) {
            if (sourceDocumentRepository.existsByUrl(request.getUrl())) {
                throw new IllegalStateException("이미 저장된 URL입니다.");
            }
        }

        SourceDocument document = new SourceDocument(
                request.getSource(),
                request.getSourceName(),
                request.getTitle(),
                request.getContent(),
                request.getUrl(),
                request.getCategory(),
                request.getAuthor(),
                request.getPostedDate()
        );
        SourceDocument savedDocument = sourceDocumentRepository.save(document);

        return new SourceDocumentResponse(savedDocument);
    }

    public List<SourceDocumentResponse> getAllDocuments(DocumentSource source, String category) {
        List<SourceDocument> documents;

        if (source != null) {
            documents = sourceDocumentRepository.findBySource(source);
        } else if (category != null && !category.isBlank()) {
            documents = sourceDocumentRepository.findByCategory(category);
        } else {
            documents = sourceDocumentRepository.findAll();
        }

        return documents.stream()
                .map(SourceDocumentResponse::new)
                .toList();
    }

    public SourceDocumentResponse getDocument(Long documentId) {
        SourceDocument document = sourceDocumentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("원본 문서를 찾을 수 없습니다. id=" + documentId));

        return new SourceDocumentResponse(document);
    }

    public AiDocumentIndexResponse indexDocument(Long documentId) {
        SourceDocument document = sourceDocumentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("원본 문서를 찾을 수 없습니다. id=" + documentId));

        AiDocumentIndexRequest request = new AiDocumentIndexRequest(
                document.getId(),
                document.getSource().name(),
                document.getSourceName(),
                document.getTitle(),
                document.getContent(),
                document.getUrl(),
                document.getCategory()
        );

        return aiServerClient.indexDocument(request);
    }

    public SourceDocumentIndexAllResponse indexAllDocuments() {
        List<SourceDocument> documents = sourceDocumentRepository.findAll();

        int totalCount = documents.size();
        int successCount = 0;
        int failCount = 0;

        for (SourceDocument document : documents) {
            try {
                AiDocumentIndexRequest request = new AiDocumentIndexRequest(
                        document.getId(),
                        document.getSource().name(),
                        document.getSourceName(),
                        document.getTitle(),
                        document.getContent(),
                        document.getUrl(),
                        document.getCategory()
                );

                AiDocumentIndexResponse response = aiServerClient.indexDocument(request);

                if (Boolean.TRUE.equals(response.getIndexed())) {
                    successCount++;
                } else {
                    failCount++;
                }

            } catch (Exception e) {
                failCount++;
                System.out.println("문서 적재 실패 documentId = " + document.getId());
                System.out.println("실패 이유 = " + e.getMessage());
            }
        }

        return new SourceDocumentIndexAllResponse(
                totalCount,
                successCount,
                failCount,
                "전체 문서 AI 서버 적재 요청이 완료되었습니다."
        );
    }
}