package com.kumoh.civilai.service;

import com.kumoh.civilai.domain.inquiry.Inquiry;
import com.kumoh.civilai.domain.inquiry.InquiryRepository;
import com.kumoh.civilai.domain.inquiry.InquiryStatus;
import com.kumoh.civilai.domain.member.Member;
import com.kumoh.civilai.domain.member.MemberRepository;
import com.kumoh.civilai.domain.member.Role;
import com.kumoh.civilai.dto.inquiry.InquiryCreateRequest;
import com.kumoh.civilai.dto.inquiry.InquiryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public InquiryResponse createInquiry(Long memberId, InquiryCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. id=" + memberId));

        if (member.getRole() != Role.STUDENT) {
            throw new IllegalStateException("학생만 문의를 등록할 수 있습니다.");
        }

        Inquiry inquiry = new Inquiry(member, request.getContent());

        Inquiry savedInquiry = inquiryRepository.save(inquiry);
        return new InquiryResponse(savedInquiry);
    }

    public List<InquiryResponse> getMyInquiries(Long memberId) {
        return inquiryRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(InquiryResponse::new)
                .toList();
    }

    public List<InquiryResponse> getAllInquiries() {
        return inquiryRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(InquiryResponse::new)
                .toList();
    }

    public InquiryResponse getInquiry(Long inquiryId, Long memberId, String role) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다. id=" + inquiryId));

        if ("STUDENT".equals(role) && !inquiry.isOwner(memberId)) {
            throw new IllegalStateException("본인의 문의만 조회할 수 있습니다.");
        }

        return new InquiryResponse(inquiry);
    }

    @Transactional
    public InquiryResponse updateStatus(Long inquiryId, InquiryStatus status) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다. id=" + inquiryId));

        inquiry.updateStatus(status);
        return new InquiryResponse(inquiry);
    }
}