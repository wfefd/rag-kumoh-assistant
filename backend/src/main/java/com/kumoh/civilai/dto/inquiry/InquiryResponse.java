package com.kumoh.civilai.dto.inquiry;

import com.kumoh.civilai.domain.inquiry.Inquiry;
import com.kumoh.civilai.domain.inquiry.InquiryStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InquiryResponse {

    private final Long id;
    private final Long memberId;
    private final String studentName;
    private final String studentNumber;
    private final String content;
    private final String category;
    private final InquiryStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public InquiryResponse(Inquiry inquiry) {
        this.id = inquiry.getId();
        this.memberId = inquiry.getMember() != null
                ? inquiry.getMember().getId()
                : null;
        this.studentName = inquiry.getStudentName();
        this.studentNumber = inquiry.getStudentNumber();
        this.content = inquiry.getContent();
        this.category = inquiry.getCategory();
        this.status = inquiry.getStatus();
        this.createdAt = inquiry.getCreatedAt();
        this.updatedAt = inquiry.getUpdatedAt();
    }
}