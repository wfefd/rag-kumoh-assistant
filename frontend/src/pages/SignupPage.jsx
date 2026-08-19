import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/api";

function SignupPage() {
    const navigate = useNavigate();

    const [form, setForm] = useState({
        loginId: "",
        password: "",
        name: "",
        studentNumber: "",
        role: "STUDENT",
    });

    const [errorMessage, setErrorMessage] = useState("");

    const changeForm = (e) => {
        const { name, value } = e.target;

        setForm((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const signup = async () => {
        if (!form.loginId.trim() || !form.password.trim() || !form.name.trim()) {
            setErrorMessage("아이디, 비밀번호, 이름은 필수입니다.");
            return;
        }

        try {
            await api.post("/api/auth/signup", {
                loginId: form.loginId,
                password: form.password,
                name: form.name,
                studentNumber: form.role === "STUDENT" ? form.studentNumber : null,
                role: form.role,
            });

            alert("회원가입이 완료되었습니다.");
            navigate("/login");
        } catch (error) {
            console.error(error);
            setErrorMessage(
                error.response?.data?.message || "회원가입에 실패했습니다."
            );
        }
    };

    return (
        <main className="login-layout">
            <div className="login-card">
                <div className="login-logo">Kumoh</div>

                <h1>회원가입</h1>
                <p>사용자 정보를 입력하세요.</p>

                <div className="login-role-group">
                    <button
                        type="button"
                        className={form.role === "STUDENT" ? "role-button active" : "role-button"}
                        onClick={() =>
                            setForm((prev) => ({ ...prev, role: "STUDENT" }))
                        }
                    >
                        학생
                    </button>

                    <button
                        type="button"
                        className={form.role === "ADMIN" ? "role-button active" : "role-button"}
                        onClick={() =>
                            setForm((prev) => ({ ...prev, role: "ADMIN" }))
                        }
                    >
                        관리자
                    </button>
                </div>

                <div className="login-form">
                    <input
                        className="login-input"
                        name="loginId"
                        placeholder="아이디"
                        value={form.loginId}
                        onChange={changeForm}
                    />

                    <input
                        className="login-input"
                        name="password"
                        type="password"
                        placeholder="비밀번호"
                        value={form.password}
                        onChange={changeForm}
                    />

                    <input
                        className="login-input"
                        name="name"
                        placeholder="이름"
                        value={form.name}
                        onChange={changeForm}
                    />

                    {form.role === "STUDENT" && (
                        <input
                            className="login-input"
                            name="studentNumber"
                            placeholder="학번"
                            value={form.studentNumber}
                            onChange={changeForm}
                        />
                    )}
                </div>

                {errorMessage && (
                    <p className="login-error">{errorMessage}</p>
                )}

                <button className="login-button" onClick={signup}>
                    회원가입
                </button>

                <button
                    className="signup-link-button"
                    onClick={() => navigate("/login")}
                >
                    로그인으로 돌아가기
                </button>
            </div>
        </main>
    );
}

export default SignupPage;