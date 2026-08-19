import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/api";

function StudentInquiryCreate() {
    const navigate = useNavigate();

    const [studentName, setStudentName] = useState("김형규");
    const [studentNumber, setStudentNumber] = useState("20200369");
    const [content, setContent] = useState("등록금 추가 납부 기간이 언제인가요?");
    const [loading, setLoading] = useState(false);

    const submitInquiry = async () => {
        if (!content.trim()) {
            alert("문의 내용을 입력하세요.");
            return;
        }

        try {
            setLoading(true);

            const response = await api.post("/api/inquiries", {
                studentName,
                studentNumber,
                content,
            });

            const inquiryId = response.data.id;

            alert(`문의가 등록되었습니다. 문의 ID: ${inquiryId}`);
            navigate(`/student/inquiries/${inquiryId}`);
        } catch (error) {
            console.error(error);
            alert(error.response?.data?.message || "문의 등록에 실패했습니다.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <main className="student-layout">
            <div className="card">
                <h2>학생 문의 등록</h2>
                <p className="description">
                    학생이 학사 행정 관련 문의를 등록하는 화면입니다.
                </p>

                <label>학생 이름</label>
                <input
                    value={studentName}
                    onChange={(e) => setStudentName(e.target.value)}
                />

                <label>학번</label>
                <input
                    value={studentNumber}
                    onChange={(e) => setStudentNumber(e.target.value)}
                />

                <label>문의 내용</label>
                <textarea
                    value={content}
                    onChange={(e) => setContent(e.target.value)}
                    placeholder="문의 내용을 입력하세요."
                />

                <button onClick={submitInquiry} disabled={loading}>
                    {loading ? "등록 중..." : "문의 등록"}
                </button>
            </div>

            <div className="card empty-box">
                <h2>문의 상태 확인</h2>
                <p>문의 등록 후 자동으로 상태 확인 화면으로 이동합니다.</p>
            </div>
        </main>
    );
}

export default StudentInquiryCreate;