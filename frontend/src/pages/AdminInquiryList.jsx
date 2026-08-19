import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/api";

function AdminInquiryList() {
    const [inquiries, setInquiries] = useState([]);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const fetchInquiries = async () => {
        try {
            setLoading(true);
            const response = await api.get("/api/inquiries");
            setInquiries(response.data);
        } catch (error) {
            console.error(error);
            alert(error.response?.data?.message || "문의 목록을 불러오지 못했습니다.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchInquiries();
    }, []);

    return (
        <div className="card">
            <div className="card-header">
                <h2>관리자 문의 목록</h2>
                <button onClick={fetchInquiries}>새로고침</button>
            </div>

            {loading && <p>불러오는 중...</p>}

            {!loading && inquiries.length === 0 && (
                <p className="muted">등록된 문의가 없습니다.</p>
            )}

            <div className="inquiry-list">
                {inquiries.map((inquiry) => (
                    <button
                        key={inquiry.id}
                        className="inquiry-item"
                        onClick={() => navigate(`/admin/inquiries/${inquiry.id}`)}
                    >
                        <div className="inquiry-title">
                            #{inquiry.id} {inquiry.category || "미분류"}
                        </div>

                        <div className="inquiry-content">{inquiry.content}</div>

                        <div className={`status status-${inquiry.status}`}>
                            {inquiry.status}
                        </div>
                    </button>
                ))}
            </div>
        </div>
    );
}

export default AdminInquiryList;