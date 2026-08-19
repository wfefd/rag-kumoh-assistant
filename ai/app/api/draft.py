from fastapi import APIRouter

from app.schemas.request import DraftRequest
from app.schemas.response import DraftResponse
from app.services.classify_service import classify_inquiry
from app.services.draft_cache_service import draft_cache_service
from app.services.generate_service import generate_draft_answer
from app.services.preprocess_service import preprocess_inquiry
from app.services.retrieve_service import retrieve_references


router = APIRouter(prefix="/ai", tags=["AI"])


@router.post("/draft", response_model=DraftResponse)
async def create_draft(request: DraftRequest) -> DraftResponse:
    normalized_question = preprocess_inquiry(
        title=request.title,
        content=request.content,
    )

    cached_result = draft_cache_service.get(normalized_question)
    if cached_result is not None:
        return DraftResponse(
            inquiry_id=request.inquiry_id,
            normalized_question=cached_result["normalized_question"],
            predicted_category=cached_result["predicted_category"],
            category_score=cached_result["category_score"],
            references=cached_result["references"],
            draft_answer=cached_result["draft_answer"],
            status=cached_result["status"],
        )

    category_result = classify_inquiry(normalized_question)

    references = retrieve_references(
        normalized_question=normalized_question,
        predicted_category=category_result["predicted_category"],
    )

    draft_answer = generate_draft_answer(
        normalized_question=normalized_question,
        predicted_category=category_result["predicted_category"],
        references=references,
    )

    if draft_answer and references:
        status = "SUCCESS"
    elif draft_answer or references:
        status = "PARTIAL_SUCCESS"
    else:
        status = "FAIL"

    draft_cache_service.set(
        normalized_question=normalized_question,
        predicted_category=category_result["predicted_category"],
        category_score=category_result["category_score"],
        references=references,
        draft_answer=draft_answer,
        status=status,
    )

    return DraftResponse(
        inquiry_id=request.inquiry_id,
        normalized_question=normalized_question,
        predicted_category=category_result["predicted_category"],
        category_score=category_result["category_score"],
        references=references,
        draft_answer=draft_answer,
        status=status,
    )
