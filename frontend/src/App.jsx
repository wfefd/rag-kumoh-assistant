import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import SignupPage from "./pages/SignupPage";
import StudentChatPage from "./pages/StudentChatPage";
import AdminChatPage from "./pages/AdminChatPage";
import ProtectedRoute from "./ProtectedRoute";
import "./App.css";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />

        <Route
          path="/student"
          element={
            <ProtectedRoute allowedRole="STUDENT">
              <StudentChatPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/student/inquiries/:id"
          element={
            <ProtectedRoute allowedRole="STUDENT">
              <StudentChatPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/admin"
          element={
            <ProtectedRoute allowedRole="ADMIN">
              <AdminChatPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/admin/inquiries/:id"
          element={
            <ProtectedRoute allowedRole="ADMIN">
              <AdminChatPage />
            </ProtectedRoute>
          }
        />

        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;