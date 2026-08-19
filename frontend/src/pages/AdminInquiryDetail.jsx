import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import api from "../api/api";

function AdminInquiryDetail({ onUpdated }) {
    const { id } = useParams();
    const inquiryId = id;

    const [inquiry, setInquiry] = useState(null);
    const [aiDraft, setAiDraft] = useState(null);
    const [finalAnswer, setFinalAnswer] = useState("");
    const [reviewerName, setReviewerName] = useState("교직원A");
    const [similarAnswers, setSimilarAnswers] = useState([]);
    const [loading, setLoading] = useState(false);
    const [similarLoading, setSimilarLoading] = useState(false);

    const fetchInquiry = async () => {
        const response = await api.get(`/api/inquiries/${inquiryId}`);
        setInquiry(response.data);
        return response.data;
    };

    const fetchAiDraft = async () => {
        try {
            const response = await api.get(
                `/api/inquiries/${inquiryId}/ai-recommendation`
            );

            setAiDraft(response.data);

            if (response.data.draftAnswer) {
                setFinalAnswer(response.data.draftAnswer);
            }
        } catch (error) {
            setAiDraft(null);
        }
    };

    const fetchSimilarAnswers = async () => {
        try {
            setSimilarLoading(true);

            const start = performance.now();

            const response = await api.get(
                `/api/inquiries/${inquiryId}/similar-answers`
            );

            const end = performance.now();
            const elapsed = ((end - start) / 1000).toFixed(2);

            console.log(`반복 민원 추천 응답 시간: ${elapsed}초`);

            setSimilarAnswers(response.data.results || []);
        } catch (error) {
            console.error(error);
            setSimilarAnswers([]);
        } finally {
            setSimilarLoading(false);
        }
    };
    const loadData = async () => {
        try {
            setLoading(true);

            await fetchInquiry();
            await fetchAiDraft();
            await fetchSimilarAnswers();
        } catch (error) {
            console.error(error);
            alert(error.response?.data?.message || "문의 상세 조회에 실패했습니다.");
        } finally {
            setLoading(false);
        }
    };

    const createAiDraft = async () => {
        try {
            setLoading(true);

            const start = performance.now();

            const response = await api.post(
                `/api/inquiries/${inquiryId}/ai-recommendation`
            );

            const end = performance.now();
            const elapsed = ((end - start) / 1000).toFixed(2);

            console.log(`AI 답변 초안 생성 시간: ${elapsed}초`);

            setAiDraft(response.data);
            setFinalAnswer(response.data.draftAnswer || "");

            await fetchInquiry();

            if (onUpdated) {
                onUpdated();
            }

            alert(`AI 답변 초안이 생성되었습니다. 소요 시간: ${elapsed}초`);
        } catch (error) {
            console.error(error);
            alert(error.response?.data?.message || "AI 초안 생성에 실패했습니다.");
        } finally {
            setLoading(false);
        }
    };

    const approveAnswer = async () => {
        if (!finalAnswer.trim()) {
            alert("최종 답변 내용을 입력하세요.");
            return;
        }

        if (!reviewerName.trim()) {
            alert("검토자 이름을 입력하세요.");
            return;
        }

        try {
            setLoading(true);

            await api.post(`/api/inquiries/${inquiryId}/answers/approve`, {
                finalAnswer,
                reviewerName,
            });

            await fetchInquiry();
            await fetchSimilarAnswers();

            if (onUpdated) {
                onUpdated();
            }

            alert("최종 답변이 승인되었습니다.");
        } catch (error) {
            console.error(error);
            alert(error.response?.data?.message || "최종 답변 승인에 실패했습니다.");
        } finally {
            setLoading(false);
        }
    };

    const useSimilarAnswer = (answer) => {
        setFinalAnswer(answer || "");
    };

    const appendSimilarAnswer = (answer) => {
        if (!answer) return;

        setFinalAnswer((prev) => {
            if (!prev.trim()) {
                return answer;
            }

            return `${prev.trim()}\n\n${answer}`;
        });
    };

    const copyText = async (text) => {
        try {
            await navigator.clipboard.writeText(text || "");
            alert("복사되었습니다.");
        } catch (error) {
            alert("복사에 실패했습니다.");
        }
    };

    useEffect(() => {
        if (inquiryId) {
            setFinalAnswer("");
            setAiDraft(null);
            setSimilarAnswers([]);
            loadData();
        }
    }, [inquiryId]);

    if (loading && !inquiry) {
        return (
            <div className="chat-detail-loading">
                <div className="sidebar-skeleton wide" />
                <div className="sidebar-skeleton wide" />
                <div className="sidebar-skeleton wide" />
            </div>
        );
    }

    if (!inquiry) {
        return (
            <div className="chat-empty">
                <h2>문의 정보를 찾을 수 없습니다.</h2>
            </div>
        );
    }

    const isCompleted = inquiry.status === "COMPLETED";

    return (
        <div className="admin-chat-detail">
            <div className="chat-detail-header">
                <div>
                    <h2>문의 #{inquiry.id}</h2>
                    <p>
                        {inquiry.studentName} · {inquiry.studentNumber} ·{" "}
                        {inquiry.category || "미분류"}
                    </p>
                </div>

                <span className={`status status-${inquiry.status}`}>
                    {inquiry.status}
                </span>
            </div>

            <div className="admin-detail-grid">
                <section className="chat-conversation">
                    <div className="message-row user-message">
                        <div className="message-avatar">학</div>
                        <div className="message-content">
                            <div className="message-name">학생 질문</div>
                            <div className="message-bubble">{inquiry.content}</div>

                            {!aiDraft && !isCompleted && (
                                <button
                                    className="inline-ai-button"
                                    onClick={createAiDraft}
                                    disabled={loading}
                                >
                                    {loading ? "AI 답변 초안 생성 중..." : "AI 답변 초안 생성"}
                                </button>
                            )}
                        </div>
                    </div>

                    {aiDraft && (
                        <div className="message-row ai-message">
                            <div className="message-avatar">AI</div>
                            <div className="message-content">
                                <div className="message-name">
                                    AI 답변 초안
                                    <button
                                        className="copy-button"
                                        onClick={() => copyText(aiDraft.draftAnswer)}
                                    >
                                        복사
                                    </button>
                                </div>

                                <div className="message-bubble">{aiDraft.draftAnswer}</div>

                                <div className="reference-box">
                                    <div>
                                        <strong>AI 분류</strong>
                                        <span>{aiDraft.category}</span>
                                    </div>
                                    <div>
                                        <strong>신뢰도</strong>
                                        <span>{aiDraft.confidence}</span>
                                    </div>
                                    <div>
                                        <strong>근거 요약</strong>
                                        <pre>{aiDraft.sourceSummary}</pre>
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}

                    <div className="message-row admin-message">
                        <div className="message-avatar">관</div>
                        <div className="message-content">
                            <div className="message-name">교직원 최종 답변</div>

                            {isCompleted && (
                                <div className="notice-box">
                                    이 문의는 이미 최종 답변이 승인되었습니다.
                                </div>
                            )}

                            <label>검토자 이름</label>
                            <input
                                value={reviewerName}
                                onChange={(e) => setReviewerName(e.target.value)}
                                disabled={isCompleted}
                            />

                            <label>최종 답변 내용</label>
                            <textarea
                                value={finalAnswer}
                                onChange={(e) => setFinalAnswer(e.target.value)}
                                disabled={isCompleted}
                                placeholder="직접 답변을 작성하거나, AI 초안 또는 유사 민원 추천 답변을 참고하세요."
                            />

                            <div className="answer-actions">
                                <button
                                    className="copy-button"
                                    onClick={() => copyText(finalAnswer)}
                                    disabled={!finalAnswer}
                                >
                                    최종 답변 복사
                                </button>

                                <button
                                    className="approve-button"
                                    onClick={approveAnswer}
                                    disabled={isCompleted || loading || !finalAnswer.trim()}
                                >
                                    {isCompleted ? "이미 승인 완료" : "최종 답변 승인"}
                                </button>
                            </div>
                        </div>
                    </div>
                </section>

                <aside className="reusable-panel">
                    <div className="reusable-panel-header">
                        <div>
                            <h3>유사 민원 추천 답변</h3>
                            <p>현재 문의와 비슷한 과거 교직원 답변입니다.</p>
                        </div>
                        <button onClick={fetchSimilarAnswers} disabled={similarLoading}>
                            {similarLoading ? "조회 중" : "새로고침"}
                        </button>
                    </div>

                    {similarLoading && (
                        <div className="reusable-empty">
                            유사 답변을 불러오는 중입니다.
                        </div>
                    )}

                    {!similarLoading && similarAnswers.length === 0 && (
                        <div className="reusable-empty">
                            유사한 과거 답변이 없습니다.
                            <br />
                            최종 답변을 승인하면 이후 추천 답변으로 활용될 수 있습니다.
                        </div>
                    )}

                    <div className="reusable-list">
                        {similarAnswers.map((item, index) => (
                            <div key={`${item.historyId}-${index}`} className="reusable-card">
                                <div className="reusable-rank">추천 {index + 1}</div>

                                <div className="reusable-category">
                                    {item.category || "일반"} · 유사도{" "}
                                    {Math.round((item.score || 0) * 100)}%
                                </div>

                                <div className="reusable-question">
                                    <strong>유사 질문</strong>
                                    <p>{item.question || "질문 정보 없음"}</p>
                                </div>

                                <div className="reusable-answer">
                                    <strong>교직원 답변</strong>
                                    <p>{item.answer}</p>
                                </div>

                                <div className="reusable-actions">
                                    <button onClick={() => useSimilarAnswer(item.answer)}>
                                        답변 사용
                                    </button>
                                    <button onClick={() => appendSimilarAnswer(item.answer)}>
                                        아래 추가
                                    </button>
                                    <button onClick={() => copyText(item.answer)}>
                                        복사
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                </aside>
            </div>
        </div>
    );
}

export default AdminInquiryDetail;