package com.kumoh.civilai.domain.document;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SourceDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 문서 출처 유형
     * 예: NOTICE, QNA, EXTERNAL
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentSource source;

    /**
     * 실제 출처 이름
     * 예: 금오공과대학교, 병무청, 한국장학재단
     */
    private String sourceName;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 1000, unique = true)
    private String url;

    /**
     * 1차 분류
     * 예: 등록금, 장학, 휴복학, 병무, 기타
     */
    private String category;

    /**
     * 데이터 수집 시각
     */
    private LocalDateTime collectedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String author;

    @Column(length = 50)
    private String postedDate;
    public SourceDocument(
            DocumentSource source,
            String sourceName,
            String title,
            String content,
            String url,
            String category,
            String author,
            String postedDate
    ) {
        this.source = source;
        this.sourceName = sourceName;
        this.title = title;
        this.content = content;
        this.url = url;
        this.category = category;
        this.author = author;
        this.postedDate = postedDate;
        this.collectedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}