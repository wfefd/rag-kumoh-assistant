import { Navigate } from "react-router-dom";

function ProtectedRoute({ allowedRole, children }) {
    const token = localStorage.getItem("accessToken");
    const isLoggedIn = localStorage.getItem("isLoggedIn") === "true";
    const role = localStorage.getItem("role");

    if (!token || !isLoggedIn) {
        return <Navigate to="/login" replace />;
    }

    if (allowedRole && role !== allowedRole) {
        return <Navigate to="/login" replace />;
    }

    return children;
}

export default ProtectedRoute;