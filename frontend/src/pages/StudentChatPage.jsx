import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import api from "../api/api";
import Sidebar from "../components/Sidebar";

function StudentChatPage() {
    const navigate = useNavigate();
    const { id } = useParams();

    const [inquiries, setInquiries] = useState([]);
    const [filteredInquiries, setFilteredInquiries] = useState([]);

    const studentName = localStorage.getItem("name") || "학생";
    const studentNumber = localStorage.getItem("studentNumber") || "";
    const [content, setContent] = useState("");

    const [selectedInquiry, setSelectedInquiry] = useState(null);
    const [answer, setAnswer] = useState(null);

    const [listLoading, setListLoading] = useState(false);
    const [detailLoading, setDetailLoading] = useState(false);
    const [submitLoading, setSubmitLoading] = useState(false);

    const fetchInquiries = async () => {
        try {
            setListLoading(true);

            const response = await api.get("/api/inquiries/my");

            setInquiries(response.data);
            setFilteredInquiries(response.data);
        } catch (error) {
            console.error(error);
            alert(error.response?.data?.message || "문의 목록을 불러오지 못했습니다.");
        } finally {
            setListLoading(false);
        }
    };

    const fetchInquiryDetail = async (inquiryId) => {
        if (!inquiryId) {
            setSelectedInquiry(null);
            setAnswer(null);
            return;
        }

        try {
            setDetailLoading(true);

            const inquiryResponse = await api.get(`/api/inquiries/${inquiryId}`);
            setSelectedInquiry(inquiryResponse.data);

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
            setDetailLoading(false);
        }
    };

    const submitInquiry = async () => {
        if (!content.trim()) {
            alert("문의 내용을 입력하세요.");
            return;
        }

        try {
            setSubmitLoading(true);

            const response = await api.post("/api/inquiries", {
                content,
            });

            const inquiryId = response.data.id;

            setContent("");
            await fetchInquiries();

            navigate(`/student/inquiries/${inquiryId}`);
        } catch (error) {
            console.error(error);
            alert(error.response?.data?.message || "문의 등록에 실패했습니다.");
        } finally {
            setSubmitLoading(false);
        }
    };

    const handleSearch = (keyword) => {
        if (!keyword.trim()) {
            setFilteredInquiries(inquiries);
            return;
        }

        const lowerKeyword = keyword.toLowerCase();

        const filtered = inquiries.filter((inquiry) => {
            const inquiryContent = inquiry.content?.toLowerCase() || "";
            const category = inquiry.category?.toLowerCase() || "";
            const status = inquiry.status?.toLowerCase() || "";
            const inquiryId = String(inquiry.id);

            return (
                inquiryContent.includes(lowerKeyword) ||
                category.includes(lowerKeyword) ||
                status.includes(lowerKeyword) ||
                inquiryId.includes(lowerKeyword)
            );
        });

        setFilteredInquiries(filtered);
    };

    const handleNewQuestion = () => {
        setSelectedInquiry(null);
        setAnswer(null);
        navigate("/student");
    };

    const handleLogout = () => {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("isLoggedIn");
        localStorage.removeItem("role");
        localStorage.removeItem("memberId");
        localStorage.removeItem("loginId");
        localStorage.removeItem("name");

        navigate("/login", { replace: true });
    };

    useEffect(() => {
        fetchInquiries();
    }, []);

    useEffect(() => {
        if (id) {
            fetchInquiryDetail(id);
        } else {
            setSelectedInquiry(null);
            setAnswer(null);
        }
    }, [id]);

    return (
        <div className="chat-shell">
            <Sidebar
                role="STUDENT"
                inquiries={filteredInquiries}
                selectedInquiryId={id}
                loading={listLoading}
                onSearch={handleSearch}
                onNewQuestion={handleNewQuestion}
                onLogout={handleLogout}
                onSelectInquiry={(inquiryId) => {
                    navigate(`/student/inquiries/${inquiryId}`);
                }}
            />

            <main className="chat-main">
                {!id && (
                    <div className="student-chat-create">
                        <div className="student-chat-title">
                            <h2>무엇을 도와드릴까요?</h2>
                            <p>학사 행정 관련 문의를 남기면 교직원 검토 후 답변이 제공됩니다.</p>
                        </div>

                        <div className="student-form-card">
                            <div className="student-profile-row">
                                <div>
                                    <label>학생 이름</label>
                                    <input
                                        value={studentName}
                                        onChange={(e) => setStudentName(e.target.value)}
                                    />
                                </div>

                                <div>
                                    <label>학번</label>
                                    <input
                                        value={studentNumber}
                                        onChange={(e) => setStudentNumber(e.target.value)}
                                    />
                                </div>
                            </div>

                            <label>문의 내용</label>
                            <textarea
                                value={content}
                                onChange={(e) => setContent(e.target.value)}
                                placeholder="예: 계절학기 시작일이 언제인가요?"
                            />

                            <button
                                className="student-submit-button"
                                onClick={submitInquiry}
                                disabled={submitLoading}
                            >
                                {submitLoading ? "등록 중..." : "문의 등록"}
                            </button>
                        </div>
                    </div>
                )}

                {id && detailLoading && !selectedInquiry && (
                    <div className="chat-detail-loading">
                        <div className="sidebar-skeleton wide" />
                        <div className="sidebar-skeleton wide" />
                        <div className="sidebar-skeleton wide" />
                    </div>
                )}

                {id && selectedInquiry && (
                    <div className="student-chat-detail">
                        <div className="chat-detail-header">
                            <div>
                                <h2>문의 #{selectedInquiry.id}</h2>
                                <p>
                                    {selectedInquiry.category || "미분류"} · {selectedInquiry.status}
                                </p>
                            </div>

                            <span className={`status status-${selectedInquiry.status}`}>
                                {selectedInquiry.status}
                            </span>
                        </div>

                        <div className="chat-conversation">
                            <div className="message-row user-message">
                                <div className="message-avatar">학</div>
                                <div className="message-content">
                                    <div className="message-name">내 질문</div>
                                    <div className="message-bubble">
                                        {selectedInquiry.content}
                                    </div>
                                </div>
                            </div>

                            {selectedInquiry.status !== "COMPLETED" && (
                                <div className="message-row ai-message">
                                    <div className="message-avatar">K</div>
                                    <div className="message-content">
                                        <div className="message-name">처리 상태</div>
                                        <div className="message-bubble">
                                            아직 최종 답변이 등록되지 않았습니다.
                                            {"\n"}
                                            교직원 검토 후 답변이 제공됩니다.
                                        </div>
                                    </div>
                                </div>
                            )}

                            {answer && (
                                <div className="message-row admin-message">
                                    <div className="message-avatar">관</div>
                                    <div className="message-content">
                                        <div className="message-name">최종 답변</div>
                                        <div className="message-bubble">
                                            {answer.finalAnswer}
                                        </div>
                                        <p className="student-answer-reviewer">
                                            검토자: {answer.reviewerName}
                                        </p>
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                )}

                {id && !detailLoading && !selectedInquiry && (
                    <div className="chat-empty">
                        <h2>문의 정보를 찾을 수 없습니다.</h2>
                        <p>왼쪽 목록에서 다른 문의를 선택하세요.</p>
                    </div>
                )}
            </main>
        </div>
    );
}

export default StudentChatPage;