package com.kumoh.civilai.service;

import com.kumoh.civilai.dto.ai.AiDraftResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.kumoh.civilai.dto.ai.AiDocumentIndexRequest;
import com.kumoh.civilai.dto.ai.AiDocumentIndexResponse;
import com.kumoh.civilai.dto.ai.AiHistoryIndexRequest;
import com.kumoh.civilai.dto.ai.AiHistoryIndexResponse;
import com.kumoh.civilai.dto.ai.AiSimilarHistoryRequest;
import com.kumoh.civilai.dto.ai.AiSimilarHistoryResponse;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AiServerClient {

    private final RestTemplate restTemplate;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    public AiDraftResponse requestDraft(Long inquiryId, String question) {
        System.out.println("AI 서버 요청 inquiryId = " + inquiryId);
        System.out.println("AI 서버 요청 question = " + question);

        String url = aiServerUrl + "/ai/draft";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("inquiryId", inquiryId);
        requestBody.put("title", "학생 문의");
        requestBody.put("content", question);

        System.out.println("AI 서버 요청 url = " + url);
        System.out.println("AI 서버 요청 body = " + requestBody);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<AiDraftResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                AiDraftResponse.class
        );

        AiDraftResponse body = response.getBody();

        if (body == null) {
            throw new IllegalStateException("AI 서버 응답 body가 비어 있습니다.");
        }

        System.out.println("AI 응답 inquiryId = " + body.getInquiryId());
        System.out.println("AI 응답 normalizedQuestion = " + body.getNormalizedQuestion());
        System.out.println("AI 응답 predictedCategory = " + body.getPredictedCategory());
        System.out.println("AI 응답 categoryScore = " + body.getCategoryScore());
        System.out.println("AI 응답 draftAnswer = " + body.getDraftAnswer());
        System.out.println("AI 응답 status = " + body.getStatus());
        System.out.println("AI 응답 sourceSummary = " + body.getSourceSummary());

        return body;
    }
    public AiDocumentIndexResponse indexDocument(AiDocumentIndexRequest request) {
        String url = aiServerUrl + "/api/rag/documents";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

        HttpEntity<AiDocumentIndexRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<AiDocumentIndexResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                AiDocumentIndexResponse.class
        );

        AiDocumentIndexResponse body = response.getBody();

        if (body == null) {
            throw new IllegalStateException("AI 서버 문서 적재 응답 body가 비어 있습니다.");
        }

        return body;
    }
    public AiHistoryIndexResponse indexHistory(AiHistoryIndexRequest request) {
        String url = aiServerUrl + "/api/rag/history";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

        HttpEntity<AiHistoryIndexRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<AiHistoryIndexResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                AiHistoryIndexResponse.class
        );

        AiHistoryIndexResponse body = response.getBody();

        if (body == null) {
            throw new IllegalStateException("AI 서버 history 적재 응답 body가 비어 있습니다.");
        }

        return body;
    }
    public AiSimilarHistoryResponse findSimilarHistories(Long inquiryId, String question, Integer topK) {
        String url = aiServerUrl + "/api/rag/similar-histories";

        AiSimilarHistoryRequest request = new AiSimilarHistoryRequest(
                inquiryId,
                question,
                topK
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

        HttpEntity<AiSimilarHistoryRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<AiSimilarHistoryResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                AiSimilarHistoryResponse.class
        );

        AiSimilarHistoryResponse body = response.getBody();

        if (body == null) {
            throw new IllegalStateException("AI 서버 유사 답변 응답 body가 비어 있습니다.");
        }

        return body;
    }
}