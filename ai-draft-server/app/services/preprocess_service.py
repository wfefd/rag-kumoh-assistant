import re

from app.services.query_spell_service import build_similarity_query


def preprocess_inquiry(title: str, content: str) -> str:
    """
    Merge title and content, clean punctuation noise,
    then correct only domain-specific typos for retrieval/classification.
    """

    merged = f"{title} {content}"
    merged = merged.replace("\n", " ").replace("\r", " ")
    merged = re.sub(r"\s+", " ", merged)
    merged = re.sub(r"[^\w\s]", " ", merged)
    merged = re.sub(r"\s+", " ", merged)
    cleaned = merged.strip()

    corrected = build_similarity_query(cleaned)

    return corrected