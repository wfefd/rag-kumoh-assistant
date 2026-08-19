package com.kumoh.civilai.service;

import com.kumoh.civilai.domain.inquiry.Inquiry;
import com.kumoh.civilai.domain.inquiry.InquiryRepository;
import com.kumoh.civilai.dto.ai.AiSimilarHistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SimilarAnswerService {

    private final InquiryRepository inquiryRepository;
    private final AiServerClient aiServerClient;

    public AiSimilarHistoryResponse findSimilarAnswers(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다. id=" + inquiryId));

        return aiServerClient.findSimilarHistories(
                inquiry.getId(),
                inquiry.getContent(),
                5
        );
    }
}