package com.kumoh.civilai.controller;

import com.kumoh.civilai.dto.answer.AnswerApproveRequest;
import com.kumoh.civilai.dto.answer.AnswerHistoryResponse;
import com.kumoh.civilai.service.AnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inquiries/{inquiryId}/answers")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    @PostMapping("/approve")
    public AnswerHistoryResponse approveAnswer(
            @PathVariable Long inquiryId,
            @Valid @RequestBody AnswerApproveRequest request
    ) {
        return answerService.approveAnswer(inquiryId, request);
    }

    @GetMapping
    public AnswerHistoryResponse getAnswer(
            @PathVariable Long inquiryId,
            Authentication authentication
    ) {
        Long memberId = Long.parseLong(authentication.getName());

        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .orElse("");

        return answerService.getAnswerByInquiryId(inquiryId, memberId, role);
    }
}