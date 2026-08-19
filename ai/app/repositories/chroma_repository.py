from __future__ import annotations

import os
from pathlib import Path
from typing import Any

from app.core.config import EMBEDDING_BACKEND, REFERENCE_DATA_SOURCE


BASE_DIR = Path(__file__).resolve().parents[1]
CHROMA_PATH = BASE_DIR / "data" / "chroma_db"
DEFAULT_COLLECTION_NAME = f"reference_documents_{REFERENCE_DATA_SOURCE}_{EMBEDDING_BACKEND}_chunked"
COLLECTION_NAME = os.getenv("CHROMA_COLLECTION_NAME", DEFAULT_COLLECTION_NAME)


class ChromaRepository:
    """
    ChromaDB-backed repository using local persistent storage.
    No separate DB server is required for this mode.
    """

    def __init__(self) -> None:
        self._client: Any | None = None
        self._collection: Any | None = None

    def upsert_documents(
        self,
        ids: list[str],
        documents: list[str],
        metadatas: list[dict],
        embeddings: list[list[float]] | None = None,
    ) -> None:
        collection = self._get_collection()
        collection.upsert(
            ids=ids,
            documents=documents,
            metadatas=metadatas,
            embeddings=embeddings,
        )

    def query_similar(
        self,
        query_embedding: list[float],
        top_k: int = 3,
        where: dict | None = None,
    ) -> list[dict]:
        collection = self._get_collection()
        result = collection.query(
            query_embeddings=[query_embedding],
            n_results=top_k,
            where=where,
            include=["documents", "metadatas", "distances"],
        )

        ids = result.get("ids", [[]])[0]
        documents = result.get("documents", [[]])[0]
        metadatas = result.get("metadatas", [[]])[0]
        distances = result.get("distances", [[]])[0]

        items: list[dict] = []
        for item_id, document, metadata, distance in zip(ids, documents, metadatas, distances):
            score = self._distance_to_score(distance)
            items.append(
                {
                    "id": item_id,
                    "document": document,
                    "metadata": metadata,
                    "score": score,
                }
            )

        items.sort(key=lambda item: item["score"], reverse=True)
        return items

    def is_seeded(self) -> bool:
        collection = self._get_collection()
        return collection.count() > 0
    def count_documents(self) -> int:
        collection = self._get_collection()
        return collection.count()

    def get_collection_name(self) -> str:
        return COLLECTION_NAME

    def get_chroma_path(self) -> str:
        return str(CHROMA_PATH)

    def reset_collection(self) -> None:
        client = self._get_client()
        try:
            client.delete_collection(COLLECTION_NAME)
        except Exception:
            pass
        self._collection = None
        self._get_collection()
   
    def _get_client(self) -> Any:
        if self._client is not None:
            return self._client

        try:
            import chromadb
        except ImportError as exc:
            raise RuntimeError(
                "chromadb is not installed. Install it with: pip install -U chromadb"
            ) from exc

        CHROMA_PATH.mkdir(parents=True, exist_ok=True)
        self._client = chromadb.PersistentClient(path=str(CHROMA_PATH))
        return self._client

    def _get_collection(self) -> Any:
        if self._collection is not None:
            return self._collection

        client = self._get_client()
        self._collection = client.get_or_create_collection(name=COLLECTION_NAME)
        return self._collection

    @staticmethod
    def _distance_to_score(distance: float | None) -> float:
        if distance is None:
            return 0.0

        score = 1.0 / (1.0 + float(distance))
        return round(max(0.0, min(score, 0.99)), 2)


chroma_repository = ChromaRepository()
