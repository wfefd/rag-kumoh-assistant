import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import api from "../api/api";

function StudentInquiryStatus() {
    const { id } = useParams();
    const inquiryId = id;

    const [inquiry, setInquiry] = useState(null);
    const [answer, setAnswer] = useState(null);
    const [loading, setLoading] = useState(false);

    const fetchStatus = async () => {
        if (!inquiryId) return;

        try {
            setLoading(true);

            const inquiryResponse = await api.get(`/api/inquiries/${inquiryId}`);
            setInquiry(inquiryResponse.data);

            if (inquiryResponse.data.status === "COMPLETED") {
                const answerResponse = await api.get(`/api/inquiries/${inquiryId}/answers`);
                setAnswer(answerResponse.data);
            } else {
                setAnswer(null);
            }
        } catch (error) {
            console.error(error);
            alert(error.response?.data?.message || "문의 상태 조회에 실패했습니다.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchStatus();
    }, [inquiryId]);

    return (
        <main className="student-layout">
            <div className="card">
                <div className="card-header">
                    <h2>문의 상태 확인</h2>
                    <button onClick={fetchStatus} disabled={loading}>
                        {loading ? "조회 중..." : "새로고침"}
                    </button>
                </div>

                {loading && !inquiry && <p>문의 상태를 불러오는 중...</p>}

                {inquiry && (
                    <div className="info-box">
                        <p>
                            <strong>문의 ID:</strong> {inquiry.id}
                        </p>
                        <p>
                            <strong>상태:</strong>{" "}
                            <span className={`status status-${inquiry.status}`}>
                                {inquiry.status}
                            </span>
                        </p>
                        <p>
                            <strong>카테고리:</strong> {inquiry.category || "미분류"}
                        </p>
                        <p>
                            <strong>문의 내용:</strong>
                        </p>
                        <div className="content-box">{inquiry.content}</div>
                    </div>
                )}

                {inquiry && inquiry.status !== "COMPLETED" && (
                    <div className="notice-box">
                        아직 최종 답변이 등록되지 않았습니다. 교직원 검토 후 답변이 제공됩니다.
                    </div>
                )}

                {answer && (
                    <div className="section">
                        <h3>최종 답변</h3>
                        <div className="content-box">{answer.finalAnswer}</div>
                        <p className="muted">검토자: {answer.reviewerName}</p>
                    </div>
                )}

                {!loading && !inquiry && (
                    <div className="notice-box">
                        문의 정보를 찾을 수 없습니다.
                    </div>
                )}

                <div className="section">
                    <Link to="/" className="nav-button">
                        새 문의 등록하기
                    </Link>
                </div>
            </div>
        </main>
    );
}

export default StudentInquiryStatus;