import { useState } from "react";
import StatusBadge from "./StatusBadge";

function Sidebar({
    role,
    inquiries = [],
    selectedInquiryId,
    onSelectInquiry,
    onNewQuestion,
    onSearch,
    onLogout,
    loading,
}) {
    const [collapsed, setCollapsed] = useState(false);
    const [keyword, setKeyword] = useState("");

    const handleSearchChange = (e) => {
        const value = e.target.value;
        setKeyword(value);

        if (onSearch) {
            onSearch(value);
        }
    };

    return (
        <aside className={`sidebar ${collapsed ? "sidebar-collapsed" : ""}`}>
            <div className="sidebar-top">
                <div className="sidebar-brand-row">
                    {!collapsed && <h1 className="sidebar-brand">Kumoh</h1>}

                    <button
                        className="sidebar-toggle"
                        onClick={() => setCollapsed((prev) => !prev)}
                    >
                        {collapsed ? "☰" : "‹"}
                    </button>
                </div>

                {!collapsed && (
                    <>
                        {role === "STUDENT" && (
                            <button className="sidebar-main-button" onClick={onNewQuestion}>
                                ＋ 새 질문
                            </button>
                        )}

                        <div className="sidebar-search">
                            <span>⌕</span>
                            <input
                                value={keyword}
                                onChange={handleSearchChange}
                                placeholder="채팅 검색"
                            />
                        </div>

                        <div className="sidebar-section-title">최근</div>
                    </>
                )}
            </div>

            {!collapsed && (
                <div className="sidebar-list">
                    {loading &&
                        Array.from({ length: 6 }).map((_, index) => (
                            <div key={index} className="sidebar-skeleton" />
                        ))}

                    {!loading &&
                        inquiries.map((inquiry) => (
                            <button
                                key={inquiry.id}
                                className={`sidebar-item ${String(selectedInquiryId) === String(inquiry.id)
                                    ? "selected"
                                    : ""
                                    }`}
                                onClick={() => onSelectInquiry(inquiry.id)}
                            >
                                <div className="sidebar-item-title">{inquiry.content}</div>

                                <div className="sidebar-item-meta">
                                    <span>#{inquiry.id}</span>
                                    <StatusBadge status={inquiry.status} />
                                </div>
                            </button>
                        ))}
                </div>
            )}

            {!collapsed && (
                <div className="sidebar-footer">
                    <div className="sidebar-user-avatar">
                        {role === "ADMIN" ? "관" : "학"}
                    </div>

                    <div className="sidebar-user-info">
                        <div className="sidebar-user-name">
                            {role === "ADMIN" ? "관리자" : "사용자"}
                        </div>
                        <div className="sidebar-user-role">
                            {role === "ADMIN" ? "교직원 계정" : "학생 계정"}
                        </div>
                    </div>

                    {onLogout && (
                        <button className="sidebar-logout" onClick={onLogout}>
                            로그아웃
                        </button>
                    )}
                </div>
            )}
        </aside>
    );
}

export default Sidebar;