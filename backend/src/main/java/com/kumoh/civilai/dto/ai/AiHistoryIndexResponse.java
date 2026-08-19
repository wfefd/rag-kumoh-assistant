package com.kumoh.civilai.dto.ai;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AiHistoryIndexResponse {

    private Long historyId;
    private Boolean indexed;
    private Integer chunkCount;
    private String message;
}