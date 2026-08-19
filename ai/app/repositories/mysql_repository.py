from __future__ import annotations

from typing import Any

from app.core.config import (
    MYSQL_CHARSET,
    MYSQL_DATABASE,
    MYSQL_HOST,    
    MYSQL_PASSWORD,
    MYSQL_PORT,
    MYSQL_SOURCE_POSTED_DATE_FROM,
    MYSQL_SOURCE_DOCUMENT_TABLE,
    MYSQL_USER,
)


class MySQLRepository:
    def _connect(self) -> Any:
        try:
            import pymysql
        except ImportError as exc:
            raise RuntimeError("pymysql is not installed. Install it with: pip install pymysql") from exc

        return pymysql.connect(
            host=MYSQL_HOST,
            port=MYSQL_PORT,
            user=MYSQL_USER,
            password=MYSQL_PASSWORD,
            database=MYSQL_DATABASE,
            charset=MYSQL_CHARSET,
            cursorclass=pymysql.cursors.DictCursor,
        )

    def load_reference_data(self) -> dict[str, list[dict]]:
        with self._connect() as connection:
            with connection.cursor() as cursor:
                rows = self._fetch_source_documents(cursor)
                faqs = self._to_faqs(rows)
                notices = self._to_notices(rows)
                print(
                    "Loaded SOURCE_DOCUMENT rows from MySQL: "
                    f"total={len(rows)}, qna={len(faqs)}, notice={len(notices)}"
                )
                return {
                    "faqs": faqs,
                    "notices": notices,
                    "histories": [],
                }

    def _fetch_source_documents(self, cursor: Any) -> list[dict]:
        table_name = MYSQL_SOURCE_DOCUMENT_TABLE.replace("`", "")
        cursor.execute(
            f"""
            SELECT
                `id`,
                `source`,
                `source_name`,
                `title`,
                `content`,
                `url`,
                `category`,
                `author`,
                `posted_date`
            FROM `{table_name}`
            WHERE LOWER(TRIM(`source`)) IN ('qna', 'notice')
              AND `posted_date` >= %s
            """,
            (MYSQL_SOURCE_POSTED_DATE_FROM,),
        )
        return cursor.fetchall()

    def _to_faqs(self, rows: list[dict]) -> list[dict]:
        return [
            {
                "id": row["id"],
                "category": row.get("category") or "OTHER",
                "question": row.get("title") or row.get("source_name") or "",
                "answer": row.get("content") or "",
                "keywords": self._build_keywords(row),
                "is_active": True,
                "url": row.get("url") or "",
                "author": row.get("author") or "",
                "posted_date": str(row.get("posted_date") or ""),
                "source_name": row.get("source_name") or "",
            }
            for row in rows
            if self._normalize_source(row) == "qna"
        ]

    def _to_notices(self, rows: list[dict]) -> list[dict]:
        return [
            {
                "id": row["id"],
                "category": row.get("category") or "NOTICE",
                "title": row.get("title") or row.get("source_name") or "",
                "content": row.get("content") or "",
                "keywords": self._build_keywords(row),
                "is_active": True,
                "url": row.get("url") or "",
                "author": row.get("author") or "",
                "posted_date": str(row.get("posted_date") or ""),
                "source_name": row.get("source_name") or "",
            }
            for row in rows
            if self._normalize_source(row) == "notice"
        ]

    @staticmethod
    def _normalize_source(row: dict) -> str:
        return str(row.get("source", "")).strip().lower()

    @staticmethod
    def _build_keywords(row: dict) -> list[str]:
        keywords = [row.get("source"), row.get("source_name"), row.get("category")]
        return [str(keyword) for keyword in keywords if keyword]


mysql_repository = MySQLRepository()
