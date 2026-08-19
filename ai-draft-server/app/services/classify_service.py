from typing import Dict


CATEGORY_RULES = {
    "계정/로그인 문의": ["로그인", "비밀번호", "계정", "인증", "접속"],
    "수강/학사 문의": ["수강", "수강신청", "학점", "졸업", "시간표"],
    "증명서/서류 문의": ["증명서", "서류", "제출", "발급", "재학"],
    "시스템 오류 문의": ["오류", "에러", "실패", "화면", "포털"],
    "운영시간 문의": ["운영시간", "시간", "마감", "운영", "언제"],
}


def classify_inquiry(normalized_question: str) -> Dict[str, float | str]:
    text = normalized_question.lower()

    best_category = "기타 문의"
    best_score = 0.4

    for category, keywords in CATEGORY_RULES.items():
        match_count = sum(1 for keyword in keywords if keyword in text)
        if match_count > 0:
            score = min(0.6 + match_count * 0.1, 0.99)
            if score > best_score:
                best_category = category
                best_score = score

    return {
        "predicted_category": best_category,
        "category_score": round(best_score, 2),
    }
