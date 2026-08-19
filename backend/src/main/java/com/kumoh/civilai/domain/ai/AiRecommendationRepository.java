package com.kumoh.civilai.domain.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiRecommendationRepository extends JpaRepository<AiRecommendation, Long> {

    Optional<AiRecommendation> findByInquiryId(Long inquiryId);
}