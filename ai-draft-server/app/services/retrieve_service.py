from app.repositories.chroma_repository import chroma_repository
from app.schemas.response import ReferenceItem
from app.services.embedding_service import embedding_service
from app.services.seed_service import seed_chroma_from_source


def retrieve_references(normalized_question: str, predicted_category: str) -> list[ReferenceItem]:
    seed_chroma_from_source()

    query_embedding = embedding_service.embed_query(normalized_question)

    search_results = chroma_repository.query_similar(
        query_embedding=query_embedding,
        top_k=10
    )

    references: list[ReferenceItem] = []

    for item in search_results:
        metadata = item["metadata"]
        score = item["score"]

        if metadata.get("category") == predicted_category:
            score = round(min(score + 0.1, 0.99), 2)

        references.append(
            ReferenceItem(
                source_type=metadata.get("source_type", ""),
                source_id=int(metadata.get("source_id", 0)),
                title=metadata.get("title", ""),
                score=score,
                content=item.get("document", "")
            )
        )

    references.sort(key=lambda item: item.score, reverse=True)

    return references[:5]