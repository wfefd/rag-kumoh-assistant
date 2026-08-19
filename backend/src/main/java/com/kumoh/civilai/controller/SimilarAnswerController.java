package com.kumoh.civilai.controller;

import com.kumoh.civilai.dto.ai.AiSimilarHistoryResponse;
import com.kumoh.civilai.service.SimilarAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SimilarAnswerController {

    private final SimilarAnswerService similarAnswerService;

    @GetMapping("/api/inquiries/{inquiryId}/similar-answers")
    public AiSimilarHistoryResponse findSimilarAnswers(
            @PathVariable Long inquiryId
    ) {
        return similarAnswerService.findSimilarAnswers(inquiryId);
    }
}