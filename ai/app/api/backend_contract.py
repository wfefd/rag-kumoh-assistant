from typing import Optional

from fastapi import APIRouter
from pydantic import BaseModel, ConfigDict, Field
from app.services.query_spell_service import build_similarity_query

from app.repositories.chroma_repository import chroma_repository
from app.services.embedding_service import embedding_service

router = APIRouter(prefix="/api", tags=["Backend Contract"])


class DocumentIndexRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    document_id: int = Field(..., alias="documentId")
    source: str
    source_name: Optional[str] = Field(default=None, alias="sourceName")
    title: str
    content: str
    url: Optional[str] = None
    category: Optional[str] = None


class DocumentIndexResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    document_id: int = Field(..., alias="documentId")
    indexed: bool
    chunk_count: int = Field(..., alias="chunkCount")
    message: str


class HistoryIndexRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    history_id: int = Field(..., alias="historyId")
    inquiry_id: int = Field(..., alias="inquiryId")

    # Spring에서 sourceType으로 보내도 받고, source로 보내도 받게 함
    source: str = Field(default="HISTORY", alias="sourceType")

    title: str
    content: str
    category: Optional[str] = None


class HistoryIndexResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    history_id: int = Field(..., alias="historyId")
    indexed: bool
    chunk_count: int = Field(..., alias="chunkCount")
    message: str


class SimilarHistoryRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    inquiry_id: int = Field(..., alias="inquiryId")
    question: str
    top_k: int = Field(default=5, alias="topK")


class SimilarHistoryItem(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    history_id: int = Field(..., alias="historyId")
    question: str
    answer: str
    category: Optional[str] = None
    score: float


class SimilarHistoryResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    inquiry_id: int = Field(..., alias="inquiryId")
    results: list[SimilarHistoryItem]


def split_history_document(document: str) -> tuple[str, str]:
    """
    HISTORY 문서에서 질문/답변을 분리한다.

    지원 형식 1:
    질문: ...
    답변: ...

    지원 형식 2:
    Inquiry Content: ...
    Answer Content: ...
    """
    if not document:
        return "", ""

    text = document.strip()

    # Spring /rag/history에서 들어오는 형식
    if "질문:" in text and "답변:" in text:
        question_part, answer_part = text.split("답변:", 1)
        question = question_part.replace("질문:", "").strip()
        answer = answer_part.strip()
        return question, answer

    # seed_service / reference_mapper_service에서 만든 형식
    if "Inquiry Content:" in text and "Answer Content:" in text:
        before_answer, answer_part = text.split("Answer Content:", 1)

        question = ""
        for line in before_answer.splitlines():
            if line.startswith("Inquiry Content:"):
                question = line.replace("Inquiry Content:", "").strip()
                break

        answer = answer_part.split("Keywords:", 1)[0].strip()
        return question, answer

    return "", text


@router.post("/rag/documents", response_model=DocumentIndexResponse)
async def index_document(request: DocumentIndexRequest) -> DocumentIndexResponse:
    print("문서 적재 요청 수신")
    print("documentId:", request.document_id)
    print("source:", request.source)
    print("sourceName:", request.source_name)
    print("title:", request.title)
    print("category:", request.category)
    print("url:", request.url)
    print("content preview:", request.content[:100])

    # 지금은 사용 안 하면 그대로 둬도 됨
    return DocumentIndexResponse(
        documentId=request.document_id,
        indexed=True,
        chunkCount=1,
        message="문서 적재 성공"
    )


@router.post("/rag/history", response_model=HistoryIndexResponse)
async def index_history(request: HistoryIndexRequest) -> HistoryIndexResponse:
    question, answer = split_history_document(request.content)

    document_id = f"HISTORY-{request.history_id}"
    category = request.category or "일반"

    search_document = (
        f"Source Type: HISTORY\n"
        f"Category: {category}\n"
        f"Question: {question}"
    )

    metadata = {
        "source_type": "HISTORY",
        "source_id": request.history_id,
        "inquiry_id": request.inquiry_id,
        "category": category,
        "title": request.title,
        "question": question,
        "answer": answer,
        "is_active": True,
    }

    embedding = embedding_service.embed_query(search_document)

    chroma_repository.upsert_documents(
        ids=[document_id],
        documents=[search_document],
        metadatas=[metadata],
        embeddings=[embedding],
    )

    return HistoryIndexResponse(
        historyId=request.history_id,
        indexed=True,
        chunkCount=1,
        message="history ChromaDB 적재 성공"
    )

@router.get("/rag/count")
async def get_rag_count():
    count = chroma_repository.count_documents()

    return {
        "collectionName": chroma_repository.get_collection_name(),
        "chromaPath": chroma_repository.get_chroma_path(),
        "count": count
    }


SIMILAR_HISTORY_MIN_SCORE = 0.5


def normalize_answer_for_dedup(answer: str) -> str:
    return (
        (answer or "")
        .lower()
        .replace(" ", "")
        .replace("\n", "")
        .replace(".", "")
        .replace(",", "")
        .strip()
    )


SIMILAR_HISTORY_MIN_SCORE = 0.5


@router.post("/rag/similar-histories", response_model=SimilarHistoryResponse)
async def find_similar_histories(request: SimilarHistoryRequest) -> SimilarHistoryResponse:
    search_question = build_similarity_query(request.question)

    print("original question:", request.question, flush=True)
    print("search question:", search_question, flush=True)

    query_embedding = embedding_service.embed_query(search_question)

    search_results = chroma_repository.query_similar(
        query_embedding=query_embedding,
        top_k=max(request.top_k * 4, 20),
        where={"source_type": "HISTORY"},
    )

    results: list[SimilarHistoryItem] = []

    for item in search_results:
        metadata = item.get("metadata", {})
        score = item.get("score", 0.0)

        question = metadata.get("question") or ""
        answer = metadata.get("answer") or ""

        print(
            "[similar candidate]",
            "score=", score,
            "stored_question=", question,
            "user_question=", request.question,
            flush=True,
        )

        if score < SIMILAR_HISTORY_MIN_SCORE:
            continue

        if not answer:
            continue

        results.append(
            SimilarHistoryItem(
                historyId=int(metadata.get("source_id", 0)),
                question=question,
                answer=answer,
                category=metadata.get("category"),
                score=score,
            )
        )

        if len(results) >= request.top_k:
            break

    return SimilarHistoryResponse(
        inquiryId=request.inquiry_id,
        results=results,
    )