package com.kumoh.civilai.controller;

import com.kumoh.civilai.dto.ai.AiRecommendationResponse;
import com.kumoh.civilai.service.AiRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inquiries/{inquiryId}/ai-recommendation")
@RequiredArgsConstructor
public class AiRecommendationController {

    private final AiRecommendationService aiRecommendationService;

    @PostMapping
    public AiRecommendationResponse createRecommendation(@PathVariable Long inquiryId) {
        return aiRecommendationService.createRecommendation(inquiryId);
    }

    @GetMapping
    public AiRecommendationResponse getRecommendation(@PathVariable Long inquiryId) {
        return aiRecommendationService.getRecommendationByInquiryId(inquiryId);
    }
}