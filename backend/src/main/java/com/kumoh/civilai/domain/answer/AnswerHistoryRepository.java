package com.kumoh.civilai.domain.answer;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AnswerHistoryRepository extends JpaRepository<AnswerHistory, Long> {

    Optional<AnswerHistory> findByInquiryId(Long inquiryId);

    Optional<AnswerHistory> findTopByNormalizedQuestionOrderByCreatedAtDesc(String normalizedQuestion);

    @Query("""
        SELECT
            a.normalizedQuestion,
            COUNT(a),
            MAX(a.createdAt)
        FROM AnswerHistory a
        WHERE a.normalizedQuestion IS NOT NULL
          AND a.normalizedQuestion <> ''
        GROUP BY a.normalizedQuestion
        ORDER BY COUNT(a) DESC, MAX(a.createdAt) DESC
    """)
    List<Object[]> findTopReusableQuestionGroups(Pageable pageable);
}