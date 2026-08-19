from __future__ import annotations

import json
import os
from pathlib import Path

from app.core.config import REFERENCE_DATA_SOURCE
from app.repositories.chroma_repository import chroma_repository
from app.repositories.mysql_repository import mysql_repository
from app.services.embedding_service import embedding_service
from app.services.reference_mapper_service import map_seed_data_to_chroma_documents


BASE_DIR = Path(__file__).resolve().parents[1]
DATA_DIR = BASE_DIR / "data"
UPSERT_BATCH_SIZE = int(os.getenv("CHROMA_UPSERT_BATCH_SIZE", "100"))


def _load_json(file_name: str) -> list[dict]:
    return json.loads((DATA_DIR / file_name).read_text(encoding="utf-8"))


def load_seed_data() -> dict[str, list[dict]]:
    if REFERENCE_DATA_SOURCE == "mysql":
        return mysql_repository.load_reference_data()

    return {
        "faqs": _load_json("seed_faq.json"),
        "notices": _load_json("seed_notices.json"),
        "histories": _load_json("seed_histories.json"),
    }


def seed_chroma_from_source(force: bool = False) -> int:
    if force:
        chroma_repository.reset_collection()
    elif chroma_repository.is_seeded():
        return 0

    seed_data = load_seed_data()
    documents = map_seed_data_to_chroma_documents(
        faqs=seed_data["faqs"],
        notices=seed_data["notices"],
        histories=seed_data["histories"],
    )
    print(f"Mapped source rows into {len(documents)} Chroma documents/chunks.")

    total_count = len(documents)
    print(f"Total documents to seed: {total_count}", flush=True)

    for start_index in range(0, total_count, UPSERT_BATCH_SIZE):
        batch = documents[start_index:start_index + UPSERT_BATCH_SIZE]

        print(
            f"[DEBUG] Batch start: {start_index}, batch size: {len(batch)}",
            flush=True
        )

        ids = [item["id"] for item in batch]
        contents = [item["document"] for item in batch]
        metadatas = [item["metadata"] for item in batch]

        print("[DEBUG] IDs / contents / metadatas prepared", flush=True)
        print(f"[DEBUG] First id: {ids[0]}", flush=True)
        print(f"[DEBUG] First content preview: {contents[0][:100]}", flush=True)

        try:
            print("[DEBUG] Embedding start", flush=True)

            embeddings = embedding_service.embed_documents(contents)

            print("[DEBUG] Embedding done", flush=True)

            print("[DEBUG] Chroma upsert start", flush=True)

            chroma_repository.upsert_documents(
                ids=ids,
                documents=contents,
                metadatas=metadatas,
                embeddings=embeddings,
            )

            print("[DEBUG] Chroma upsert done", flush=True)

            print(
                f"Seeded {min(start_index + len(batch), total_count)}/{total_count} documents.",
                flush=True
            )

        except Exception as e:
            print("[ERROR] Failed while seeding batch", flush=True)
            print(f"[ERROR] start_index={start_index}", flush=True)
            print(f"[ERROR] error type={type(e)}", flush=True)
            print(f"[ERROR] error message={e}", flush=True)
            raise

    return len(documents)


def seed_chroma_from_json(force: bool = False) -> int:
    return seed_chroma_from_source(force=force)
