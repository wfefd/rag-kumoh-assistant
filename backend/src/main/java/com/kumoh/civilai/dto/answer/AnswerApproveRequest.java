package com.kumoh.civilai.dto.answer;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AnswerApproveRequest {

    @NotBlank(message = "최종 답변 내용은 필수입니다.")
    private String finalAnswer;

    private String reviewerName;
}