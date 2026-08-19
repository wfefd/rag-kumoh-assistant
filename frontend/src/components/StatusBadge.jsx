function StatusBadge({ status }) {
    const labelMap = {
        RECEIVED: "NEW",
        AI_DRAFTED: "답변안함",
        COMPLETED: "답변완료",
    };

    const label = labelMap[status] || status || "NEW";

    return (
        <span className={`sidebar-status sidebar-status-${status}`}>
            {label}
        </span>
    );
}

export default StatusBadge;