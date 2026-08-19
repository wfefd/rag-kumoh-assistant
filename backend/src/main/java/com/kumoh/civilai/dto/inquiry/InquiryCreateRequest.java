package com.kumoh.civilai.dto.inquiry;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class InquiryCreateRequest {

    @NotBlank
    private String content;
}