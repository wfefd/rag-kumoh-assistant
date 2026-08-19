package com.kumoh.civilai.dto.inquiry;

import com.kumoh.civilai.domain.inquiry.InquiryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InquiryStatusUpdateRequest {

    @NotNull(message = "상태값은 필수입니다.")
    private InquiryStatus status;
}