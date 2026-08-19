from app.schemas.response import ReferenceItem
from app.clients.llm_client import build_dummy_answer


def generate_draft_answer(
        normalized_question: str,
        predicted_category: str,
        references: list[ReferenceItem]
) -> str | None:
    if not references:
        return None

    return build_dummy_answer(
        normalized_question=normalized_question,
        predicted_category=predicted_category,
        references=references
    )