package com.kumoh.civilai.controller;

import com.kumoh.civilai.dto.ai.AiDraftResponse;
import com.kumoh.civilai.service.AiServerClient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-test")
@RequiredArgsConstructor
public class AiTestController {

    private final AiServerClient aiServerClient;

    @PostMapping
    public AiDraftResponse testAiServer(@RequestBody AiTestRequest request) {
        return aiServerClient.requestDraft(request.getInquiryId(), request.getQuestion());
    }

    @Getter
    @NoArgsConstructor
    public static class AiTestRequest {
        private Long inquiryId;
        private String question;
    }
}