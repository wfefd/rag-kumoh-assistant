from __future__ import annotations

import os
import hashlib
import math
import re
from typing import Any

from app.core.config import EMBEDDING_BACKEND


MODEL_NAME = os.getenv("BGE_M3_MODEL", "BAAI/bge-m3")
BATCH_SIZE = int(os.getenv("BGE_M3_BATCH_SIZE", "8"))
MAX_LENGTH = int(os.getenv("BGE_M3_MAX_LENGTH", "1024"))
HASH_EMBEDDING_DIMENSIONS = int(os.getenv("HASH_EMBEDDING_DIMENSIONS", "384"))


class EmbeddingService:
    """
    Embedding service.
    The model is loaded lazily so the app can still import even before dependencies are installed.
    """

    def __init__(self) -> None:
        self._model: Any | None = None

    def embed_documents(self, texts: list[str]) -> list[list[float]]:
        if not texts:
            return []

        if EMBEDDING_BACKEND == "hash":
            return [self._hash_embed(text) for text in texts]

        output = self._get_model().encode(
            texts,
            batch_size=BATCH_SIZE,
            max_length=MAX_LENGTH,
        )
        dense_vectors = output["dense_vecs"]
        return self._to_python_lists(dense_vectors)

    def embed_query(self, text: str) -> list[float]:
        if EMBEDDING_BACKEND == "hash":
            return self._hash_embed(text)

        output = self._get_model().encode(
            [text],
            batch_size=1,
            max_length=MAX_LENGTH,
        )
        dense_vectors = self._to_python_lists(output["dense_vecs"])
        return dense_vectors[0] if dense_vectors else []

    def _get_model(self) -> Any:
        if self._model is not None:
            return self._model

        try:
            from FlagEmbedding import BGEM3FlagModel
        except ImportError as exc:
            raise RuntimeError(
                "FlagEmbedding is not installed. Install it with: pip install -U FlagEmbedding"
            ) from exc

        self._model = BGEM3FlagModel(
            MODEL_NAME,
            use_fp16=False,
        )
        return self._model

    @staticmethod
    def _to_python_lists(vectors: Any) -> list[list[float]]:
        if hasattr(vectors, "tolist"):
            return vectors.tolist()
        return [list(vector) for vector in vectors]

    @staticmethod
    def _tokenize(text: str) -> list[str]:
        normalized_text = re.sub(r"\s+", " ", text.lower()).strip()
        words = re.findall(r"\w+", normalized_text)
        compact_text = re.sub(r"\s+", "", normalized_text)
        char_ngrams = [
            compact_text[index:index + size]
            for size in (2, 3, 4)
            for index in range(max(len(compact_text) - size + 1, 0))
        ]
        return words + char_ngrams

    def _hash_embed(self, text: str) -> list[float]:
        vector = [0.0] * HASH_EMBEDDING_DIMENSIONS
        for token in self._tokenize(text):
            digest = hashlib.blake2b(token.encode("utf-8"), digest_size=8).digest()
            raw_value = int.from_bytes(digest, "big")
            index = raw_value % HASH_EMBEDDING_DIMENSIONS
            sign = 1.0 if raw_value & 1 else -1.0
            vector[index] += sign

        norm = math.sqrt(sum(value * value for value in vector))
        if norm == 0:
            return vector

        return [value / norm for value in vector]


embedding_service = EmbeddingService()
