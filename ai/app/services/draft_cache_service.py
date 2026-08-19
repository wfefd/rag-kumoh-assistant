from __future__ import annotations

import os
from collections import OrderedDict
from threading import Lock

from app.schemas.response import ReferenceItem


CACHE_MAX_SIZE = int(os.getenv("DRAFT_CACHE_MAX_SIZE", "128"))


class DraftCacheService:
    def __init__(self) -> None:
        self._items: OrderedDict[str, dict] = OrderedDict()
        self._lock = Lock()

    def get(self, normalized_question: str) -> dict | None:
        key = self._build_key(normalized_question)
        with self._lock:
            cached = self._items.get(key)
            if cached is None:
                return None

            self._items.move_to_end(key)
            return {
                **cached,
                "references": [
                    item.model_copy() if hasattr(item, "model_copy") else item.copy()
                    for item in cached["references"]
                ],
            }

    def set(
        self,
        normalized_question: str,
        predicted_category: str,
        category_score: float,
        references: list[ReferenceItem],
        draft_answer: str | None,
        status: str,
    ) -> None:
        key = self._build_key(normalized_question)
        with self._lock:
            self._items[key] = {
                "normalized_question": normalized_question,
                "predicted_category": predicted_category,
                "category_score": category_score,
                "references": [
                    item.model_copy() if hasattr(item, "model_copy") else item.copy()
                    for item in references
                ],
                "draft_answer": draft_answer,
                "status": status,
            }
            self._items.move_to_end(key)

            while len(self._items) > CACHE_MAX_SIZE:
                self._items.popitem(last=False)

    @staticmethod
    def _build_key(normalized_question: str) -> str:
        return " ".join(normalized_question.lower().split())


draft_cache_service = DraftCacheService()
