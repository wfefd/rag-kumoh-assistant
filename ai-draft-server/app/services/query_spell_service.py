from __future__ import annotations

import re
from difflib import SequenceMatcher

from jamo import h2j, j2hcj


DOMAIN_TERMS = [
    # 수업 / 학사 일정
    "계절학기",
    "계절수업",
    "개강일",
    "시작일",
    "수업일정",
    "수강신청",
    "수강정정",
    "수강취소",
    "폐강",
    "성적",
    "성적정정",
    "성적확정",

    # 휴보강 / 공휴일
    "휴보강",
    "휴보강계획",
    "보강",
    "보강계획",
    "보강일",
    "휴강",
    "공휴일",

    # 등록금 / 장학
    "등록금",
    "수강료",
    "납부기간",
    "추가납부",
    "환불",
    "장학금",
    "국가장학금",

    # 학적
    "휴학",
    "복학",
    "자퇴",
    "졸업",
    "졸업요건",
    "전과",
    "전공변경",

    # 학교 시스템 / 생활
    "통합정보시스템",
    "비스킷",
    "OCW",
    "기숙사",
    "생활관",
    "통학버스",
]


# 조사/어미가 붙은 형태를 줄이기 위한 후처리 후보
PARTICLE_SUFFIXES = [
    "이",
    "가",
    "은",
    "는",
    "을",
    "를",
    "에",
    "에서",
    "으로",
    "로",
    "도",
    "만",
    "부터",
    "까지",
    "인데",
    "인데요",
    "인가요",
    "이에요",
    "예요",
    "야",
]


def to_jamo_text(text: str) -> str:
    """
    한글 음절을 자모 단위 문자열로 변환한다.
    예: 계절학기 -> ㄱㅖㅈㅓㄹㅎㅏㄱㄱㅣ
    """
    return j2hcj(h2j(text or ""))


def jamo_similarity(a: str, b: str) -> float:
    """
    자모 기준 문자열 유사도.
    오타가 있는 한국어 단어 비교에 일반 글자 비교보다 조금 더 유리하다.
    """
    return SequenceMatcher(None, to_jamo_text(a), to_jamo_text(b)).ratio()


def tokenize_query(text: str) -> list[str]:
    """
    한글/영문/숫자 덩어리 단위로 토큰화한다.
    예: '개절학기가 그래서 언제시작하는데'
    -> ['개절학기가', '그래서', '언제시작하는데']
    """
    return re.findall(r"[가-힣A-Za-z0-9]+", text or "")


def strip_suffix(token: str) -> str:
    """
    도메인 단어 비교 전에 조사/어미를 약하게 제거한다.
    원문을 직접 바꾸는 게 아니라 비교 후보를 만들기 위한 용도다.
    """
    for suffix in sorted(PARTICLE_SUFFIXES, key=len, reverse=True):
        if token.endswith(suffix) and len(token) > len(suffix) + 1:
            return token[: -len(suffix)]

    return token


def find_best_domain_term(token: str) -> tuple[str | None, float]:
    """
    token과 가장 가까운 도메인 단어를 찾는다.
    """
    best_term = None
    best_score = 0.0

    candidates = {
        token,
        strip_suffix(token),
    }

    for candidate in candidates:
        if len(candidate) < 2:
            continue

        for term in DOMAIN_TERMS:
            # 이미 포함 관계면 굳이 고치지 않아도 된다.
            if term in candidate or candidate in term:
                score = 1.0
            else:
                score = jamo_similarity(candidate, term)

            if score > best_score:
                best_score = score
                best_term = term

    return best_term, best_score


def correct_domain_terms(question: str, threshold: float = 0.72) -> str:
    """
    학교 행정 도메인 단어 오타만 교정한다.

    중요:
    - 사용자 질문 원문을 DB에서 바꾸기 위한 함수가 아니다.
    - ChromaDB 유사도 검색 전에만 사용할 검색용 보정 함수다.
    """
    if not question:
        return ""

    corrected = question
    tokens = tokenize_query(question)

    for token in tokens:
        # 너무 짧은 토큰은 오탐 위험이 높으므로 제외
        if len(token) < 3:
            continue

        # 이미 도메인 단어가 들어 있으면 그대로 둔다.
        if any(term in token for term in DOMAIN_TERMS):
            continue

        best_term, best_score = find_best_domain_term(token)

        if best_term and best_score >= threshold:
            corrected = corrected.replace(token, best_term)

    return corrected


def build_similarity_query(question: str) -> str:
    """
    임베딩 검색용 질문을 만든다.
    원문 전체를 정규화하지 않고, 도메인 용어 오타만 보정한다.
    """
    corrected = correct_domain_terms(question)

    return corrected.strip()