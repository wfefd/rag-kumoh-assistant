package com.kumoh.civilai.dto.ai;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AiDocumentIndexResponse {

    private Long documentId;
    private Boolean indexed;
    private Integer chunkCount;
    private String message;
}