package com.kumoh.civilai.dto.document;

import com.kumoh.civilai.domain.document.DocumentSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SourceDocumentCreateRequest {

    @NotNull(message = "문서 출처 유형은 필수입니다.")
    private DocumentSource source;

    private String sourceName;

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "본문은 필수입니다.")
    private String content;

    private String url;

    private String category;

    private String author;

    private String postedDate;
}