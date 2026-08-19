package com.kumoh.civilai.domain.inquiry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    long countByStatus(InquiryStatus status);

    List<Inquiry> findByStatus(InquiryStatus status);

    List<Inquiry> findTop5ByOrderByCreatedAtDesc();

    List<Inquiry> findAllByOrderByCreatedAtDesc();

    List<Inquiry> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    @Query("""
        SELECT i.category, COUNT(i)
        FROM Inquiry i
        WHERE i.category IS NOT NULL
        GROUP BY i.category
        ORDER BY COUNT(i) DESC
    """)
    List<Object[]> findTopCategories(Pageable pageable);
}