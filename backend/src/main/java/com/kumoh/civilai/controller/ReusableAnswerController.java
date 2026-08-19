package com.kumoh.civilai.controller;

import com.kumoh.civilai.dto.admin.ReusableAnswerResponse;
import com.kumoh.civilai.service.ReusableAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReusableAnswerController {

    private final ReusableAnswerService reusableAnswerService;

    @GetMapping("/api/admin/reusable-answers/top")
    public List<ReusableAnswerResponse> getTopReusableAnswers() {
        return reusableAnswerService.getTopReusableAnswers();
    }

    @PostMapping("/api/admin/reusable-answers/normalize")
    public String normalizeExistingAnswerHistories() {
        reusableAnswerService.normalizeExistingAnswerHistories();
        return "answer_history normalizedQuestion 보정 완료";
    }
}