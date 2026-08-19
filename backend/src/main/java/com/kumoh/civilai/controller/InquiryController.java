package com.kumoh.civilai.controller;

import com.kumoh.civilai.dto.inquiry.InquiryCreateRequest;
import com.kumoh.civilai.dto.inquiry.InquiryResponse;
import com.kumoh.civilai.dto.inquiry.InquiryStatusUpdateRequest;
import com.kumoh.civilai.service.InquiryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping
    public InquiryResponse createInquiry(
            @Valid @RequestBody InquiryCreateRequest request,
            Authentication authentication
    ) {
        Long memberId = Long.parseLong(authentication.getName());
        return inquiryService.createInquiry(memberId, request);
    }

    // 학생용: 내 문의 목록
    @GetMapping("/my")
    public List<InquiryResponse> getMyInquiries(Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        return inquiryService.getMyInquiries(memberId);
    }

    // 관리자용: 전체 문의 목록
    @GetMapping
    public List<InquiryResponse> getAllInquiries() {
        return inquiryService.getAllInquiries();
    }

    @GetMapping("/{inquiryId}")
    public InquiryResponse getInquiry(
            @PathVariable Long inquiryId,
            Authentication authentication
    ) {
        Long memberId = Long.parseLong(authentication.getName());

        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .orElse("");

        return inquiryService.getInquiry(inquiryId, memberId, role);
    }

    @PatchMapping("/{inquiryId}/status")
    public InquiryResponse updateStatus(
            @PathVariable Long inquiryId,
            @Valid @RequestBody InquiryStatusUpdateRequest request
    ) {
        return inquiryService.updateStatus(inquiryId, request.getStatus());
    }
}