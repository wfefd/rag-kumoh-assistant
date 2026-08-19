import os

from google import genai

from app.schemas.response import ReferenceItem


MODEL_NAME = os.getenv("GEMINI_MODEL", "gemini-2.5-flash")


def _build_reference_text(references: list[ReferenceItem]) -> str:
    if not references:
        return "참고 문서 없음"

    lines: list[str] = []

    for index, reference in enumerate(references, start=1):
        content = reference.content or ""

        # 너무 긴 청크가 들어오면 프롬프트가 과하게 길어질 수 있으니 제한
        content = content[:2500]

        lines.append(
            f"[참고문서 {index}]\n"
            f"문서유형: {reference.source_type}\n"
            f"문서ID: {reference.source_id}\n"
            f"제목: {reference.title}\n"
            f"유사도점수: {reference.score}\n"
            f"본문:\n{content}"
        )

    return "\n\n".join(lines)

def build_dummy_answer(
    normalized_question: str,
    predicted_category: str,
    references: list[ReferenceItem],
) -> str:
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        raise RuntimeError("GEMINI_API_KEY is not set.")

    client = genai.Client(api_key=api_key)

    prompt = (
        "당신은 대학교 민원 답변 초안을 작성하는 AI 비서입니다.\n"
        "반드시 한국어로 정중하고 간결하게 작성하세요.\n"
        "주어진 문의 분류와 참고 문서를 바탕으로 답변하세요.\n"
        "참고 문서에 없는 사실이나 규정을 임의로 만들어내지 마세요.\n"
        "참고 문서 본문 안에 날짜, 기간, 장소, 연락처가 있으면 그 값을 우선적으로 사용하세요.\n"
        "질문과 직접 관련된 참고문서 본문을 근거로 답변하세요.\n"
        "정보가 부족하면 추가 확인이 필요하다고 안내하세요.\n\n"
        f"[문의 분류]\n{predicted_category}\n\n"
        f"[정규화된 문의]\n{normalized_question}\n\n"
        f"[참고 문서]\n{_build_reference_text(references)}\n\n"
        "위 정보를 바탕으로 사용자에게 전달할 답변 초안을 작성하세요."
    )

    try:
        response = client.models.generate_content(
            model=MODEL_NAME,
            contents=prompt,
        )
    except Exception as exc:
        raise RuntimeError(f"Gemini API call failed: {exc}") from exc

    answer = (getattr(response, "text", None) or "").strip()
    if not answer:
        raise RuntimeError("Gemini returned an empty response.")

    return answer
