import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/api";

function LoginPage() {
    const navigate = useNavigate();

    const [loginId, setLoginId] = useState("");
    const [password, setPassword] = useState("");
    const [errorMessage, setErrorMessage] = useState("");

    const login = async () => {
        if (!loginId.trim() || !password.trim()) {
            setErrorMessage("아이디와 비밀번호를 입력하세요.");
            return;
        }

        try {
            const response = await api.post("/api/auth/login", {
                loginId,
                password,
            });

            const data = response.data;

            localStorage.setItem("accessToken", data.accessToken);
            localStorage.setItem("isLoggedIn", "true");
            localStorage.setItem("role", data.role);
            localStorage.setItem("memberId", data.memberId);
            localStorage.setItem("loginId", data.loginId);
            localStorage.setItem("name", data.name);

            if (data.role === "ADMIN") {
                navigate("/admin");
            } else {
                navigate("/student");
            }
        } catch (error) {
            console.error(error);
            setErrorMessage("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
    };

    return (
        <main className="login-layout">
            <div className="login-card">
                <div className="login-logo">Kumoh</div>

                <h1>반복 민원 상담 AI 자동응답 서비스</h1>
                <p>아이디와 비밀번호를 입력하여 로그인하세요.</p>

                <div className="login-form">
                    <input
                        className="login-input"
                        type="text"
                        placeholder="아이디"
                        value={loginId}
                        onChange={(e) => setLoginId(e.target.value)}
                    />

                    <input
                        className="login-input"
                        type="password"
                        placeholder="비밀번호"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        onKeyDown={(e) => {
                            if (e.key === "Enter") {
                                login();
                            }
                        }}
                    />
                </div>

                {errorMessage && (
                    <p className="login-error">{errorMessage}</p>
                )}

                <button className="login-button" onClick={login}>
                    로그인
                </button>

                <button
                    className="signup-link-button"
                    onClick={() => navigate("/signup")}
                >
                    회원가입
                </button>
            </div>
        </main>
    );
}

export default LoginPage;