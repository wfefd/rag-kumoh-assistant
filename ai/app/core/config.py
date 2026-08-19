from __future__ import annotations

import os
from dotenv import load_dotenv

load_dotenv()


def _required_env(name: str) -> str:
    value = os.getenv(name)
    if not value:
        raise RuntimeError(f"{name} environment variable is required.")
    return value


MYSQL_HOST = _required_env("MYSQL_HOST")
MYSQL_PORT = int(os.getenv("MYSQL_PORT", "3306"))
MYSQL_USER = _required_env("MYSQL_USER")
MYSQL_PASSWORD = _required_env("MYSQL_PASSWORD")
MYSQL_DATABASE = _required_env("MYSQL_DATABASE")
MYSQL_CHARSET = os.getenv("MYSQL_CHARSET", "utf8mb4")

MYSQL_SOURCE_DOCUMENT_TABLE = os.getenv("MYSQL_SOURCE_DOCUMENT_TABLE", "SOURCE_DOCUMENT")
MYSQL_SOURCE_POSTED_DATE_FROM = os.getenv("MYSQL_SOURCE_POSTED_DATE_FROM", "2026-01-01")

REFERENCE_DATA_SOURCE = os.getenv("REFERENCE_DATA_SOURCE", "mysql").lower()
EMBEDDING_BACKEND = os.getenv("EMBEDDING_BACKEND", "bge").lower()

CHUNK_SIZE = int(os.getenv("CHUNK_SIZE", "900"))
CHUNK_OVERLAP = int(os.getenv("CHUNK_OVERLAP", "150"))
CHROMA_UPSERT_BATCH_SIZE = int(os.getenv("CHROMA_UPSERT_BATCH_SIZE", "100"))
DRAFT_CACHE_MAX_SIZE = int(os.getenv("DRAFT_CACHE_MAX_SIZE", "128"))

GEMINI_API_KEY = _required_env("GEMINI_API_KEY")
GEMINI_MODEL = os.getenv("GEMINI_MODEL", "gemini-2.5-flash")