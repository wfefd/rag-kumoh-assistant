package com.kumoh.civilai.service;

import com.kumoh.civilai.domain.answer.AnswerHistory;
import com.kumoh.civilai.domain.answer.AnswerHistoryRepository;
import com.kumoh.civilai.dto.admin.ReusableAnswerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReusableAnswerService {

    private final AnswerHistoryRepository answerHistoryRepository;

    public List<ReusableAnswerResponse> getTopReusableAnswers() {
        return answerHistoryRepository.findTopReusableQuestionGroups(PageRequest.of(0, 10))
                .stream()
                .map(row -> {
                    String normalizedQuestion = (String) row[0];
                    Long usedCount = (Long) row[1];

                    AnswerHistory latestHistory = answerHistoryRepository
                            .findTopByNormalizedQuestionOrderByCreatedAtDesc(normalizedQuestion)
                            .orElseThrow();

                    return new ReusableAnswerResponse(
                            latestHistory.getId(),
                            latestHistory.getQuestion(),
                            latestHistory.getFinalAnswer(),
                            latestHistory.getCategory() != null ? latestHistory.getCategory() : "일반",
                            usedCount
                    );
                })
                .toList();
    }

    @Transactional
    public void normalizeExistingAnswerHistories() {
        List<AnswerHistory> histories = answerHistoryRepository.findAll();

        for (AnswerHistory history : histories) {
            history.updateNormalizedQuestion();
        }
    }
}