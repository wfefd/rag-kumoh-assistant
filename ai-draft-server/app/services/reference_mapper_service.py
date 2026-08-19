from __future__ import annotations

import re

from app.core.config import CHUNK_OVERLAP, CHUNK_SIZE


def _join_keywords(keywords: list[str]) -> str:
    return ", ".join(keywords)


def _normalize_text(text: str) -> str:
    return re.sub(r"\s+", " ", text or "").strip()


def _chunk_text(text: str, chunk_size: int = CHUNK_SIZE, overlap: int = CHUNK_OVERLAP) -> list[str]:
    normalized_text = _normalize_text(text)
    if not normalized_text:
        return [""]

    if len(normalized_text) <= chunk_size:
        return [normalized_text]

    chunks: list[str] = []
    start = 0
    text_length = len(normalized_text)

    while start < text_length:
        target_end = min(start + chunk_size, text_length)
        end = target_end

        if target_end < text_length:
            window_start = max(start + chunk_size // 2, start)
            break_candidates = [
                normalized_text.rfind(marker, window_start, target_end)
                for marker in (". ", "? ", "! ")
            ]
            best_break = max(break_candidates)
            if best_break > window_start:
                end = best_break + 1
            else:
                whitespace_break = normalized_text.rfind(" ", window_start, target_end)
                if whitespace_break > window_start:
                    end = whitespace_break

        chunk = normalized_text[start:end].strip()
        if chunk:
            chunks.append(chunk)

        if end >= text_length:
            break

        start = max(end - overlap, 0)

    return chunks


def _common_metadata(item: dict, source_type: str, title: str) -> dict:
    return {
        "source_type": source_type,
        "source_id": item["id"],
        "category": item["category"],
        "title": title,
        "is_active": item["is_active"],
        "url": item.get("url", ""),
        "author": item.get("author", ""),
        "posted_date": item.get("posted_date", ""),
        "source_name": item.get("source_name", ""),
    }


def faq_to_chroma_document(item: dict) -> list[dict]:
    chunks = _chunk_text(item["answer"])
    chunked_documents: list[dict] = []
    for index, chunk in enumerate(chunks):
        metadata = _common_metadata(item, source_type="FAQ", title=item["question"])
        metadata.update(
            {
                "parent_id": f"FAQ-{item['id']}",
                "chunk_index": index,
                "chunk_count": len(chunks),
            }
        )
        chunked_documents.append(
            {
                "id": f"FAQ-{item['id']}-CHUNK-{index}",
                "document": (
                    f"Source Type: FAQ\n"
                    f"Category: {item['category']}\n"
                    f"Question: {item['question']}\n"
                    f"Answer Chunk: {chunk}\n"
                    f"Keywords: {_join_keywords(item['keywords'])}"
                ),
                "metadata": metadata,
            }
        )
    return chunked_documents


def notice_to_chroma_document(item: dict) -> list[dict]:
    chunks = _chunk_text(item["content"])
    chunked_documents: list[dict] = []
    for index, chunk in enumerate(chunks):
        metadata = _common_metadata(item, source_type="NOTICE", title=item["title"])
        metadata.update(
            {
                "parent_id": f"NOTICE-{item['id']}",
                "chunk_index": index,
                "chunk_count": len(chunks),
            }
        )
        chunked_documents.append(
            {
                "id": f"NOTICE-{item['id']}-CHUNK-{index}",
                "document": (
                    f"Source Type: NOTICE\n"
                    f"Category: {item['category']}\n"
                    f"Title: {item['title']}\n"
                    f"Content Chunk: {chunk}\n"
                    f"Keywords: {_join_keywords(item['keywords'])}"
                ),
                "metadata": metadata,
            }
        )
    return chunked_documents


def history_to_chroma_document(item: dict) -> dict:
    question = item.get("inquiry_content", "")
    answer = item.get("answer_content", "")
    category = item.get("category") or "일반"
    title = item.get("inquiry_title") or f"{category} 문의 최종 답변"

    search_document = (
        f"Source Type: HISTORY\n"
        f"Category: {category}\n"
        f"Question: {question}"
    )

    return {
        "id": f"HISTORY-{item['id']}",
        "document": search_document,
        "metadata": {
            "source_type": "HISTORY",
            "source_id": item["id"],
            "category": category,
            "title": title,
            "is_active": item.get("is_active", True),
            "question": question,
            "answer": answer,
        },
    }

def map_seed_data_to_chroma_documents(
    faqs: list[dict],
    notices: list[dict],
    histories: list[dict],
) -> list[dict]:
    documents: list[dict] = []
    for item in faqs:
        if item.get("is_active", True):
            documents.extend(faq_to_chroma_document(item))
    for item in notices:
        if item.get("is_active", True):
            documents.extend(notice_to_chroma_document(item))
    documents.extend(history_to_chroma_document(item) for item in histories if item.get("is_active", True))
    return documents
