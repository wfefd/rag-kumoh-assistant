from app.services.seed_service import seed_chroma_from_source


def main() -> None:
    inserted_count = seed_chroma_from_source(force=True)
    print(f"Seeded {inserted_count} reference documents into the Chroma collection file.")


if __name__ == "__main__":
    main()