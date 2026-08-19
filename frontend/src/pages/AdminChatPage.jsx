import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import api from "../api/api";
import Sidebar from "../components/Sidebar";
import AdminInquiryDetail from "./AdminInquiryDetail";

function AdminChatPage() {
    const navigate = useNavigate();
    const { id } = useParams();

    const [inquiries, setInquiries] = useState([]);
    const [filteredInquiries, setFilteredInquiries] = useState([]);
    const [loading, setLoading] = useState(false);

    const fetchInquiries = async () => {
        try {
            setLoading(true);
            const response = await api.get("/api/inquiries");
            setInquiries(response.data);
            setFilteredInquiries(response.data);
        } catch (error) {
            console.error(error);
            alert("문의 목록을 불러오지 못했습니다.");
        } finally {
            setLoading(false);
        }
    };

    const handleSearch = (keyword) => {
        if (!keyword.trim()) {
            setFilteredInquiries(inquiries);
            return;
        }

        const lowerKeyword = keyword.toLowerCase();

        setFilteredInquiries(
            inquiries.filter((inquiry) =>
                inquiry.content?.toLowerCase().includes(lowerKeyword) ||
                inquiry.category?.toLowerCase().includes(lowerKeyword) ||
                String(inquiry.id).includes(lowerKeyword)
            )
        );
    };
    const handleLogout = () => {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("isLoggedIn");
        localStorage.removeItem("role");
        localStorage.removeItem("memberId");
        localStorage.removeItem("loginId");
        localStorage.removeItem("name");
        navigate("/login");
    };
    useEffect(() => {
        fetchInquiries();
    }, []);

    return (
        <div className="chat-shell">
            <Sidebar
                role="ADMIN"
                inquiries={filteredInquiries}
                selectedInquiryId={id}
                loading={loading}
                onSearch={handleSearch}
                onSelectInquiry={(inquiryId) => {
                    navigate(`/admin/inquiries/${inquiryId}`);
                }}
                onLogout={handleLogout}
            />
            <main className="chat-main">
                {id ? (
                    <AdminInquiryDetail inquiryIdFromRoute={id} />
                ) : (
                    <div className="chat-empty">
                        <h2>Kumoh 관리자 상담 화면</h2>
                        <p>왼쪽에서 처리할 문의를 선택하세요.</p>
                    </div>
                )}
            </main>
        </div>
    );
}

export default AdminChatPage;