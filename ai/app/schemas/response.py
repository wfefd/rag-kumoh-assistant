from typing import List, Literal, Optional

from pydantic import BaseModel, ConfigDict, Field


class ReferenceItem(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    source_type: str = Field(..., alias="sourceType", description="Reference source type")
    source_id: int = Field(..., alias="sourceId", description="Reference source ID")
    title: str = Field(..., description="Reference title")
    score: float = Field(..., ge=0.0, le=1.0, description="Reference relevance score")
    content: Optional[str] = Field(default=None, description="Reference chunk content")

class DraftResponse(BaseModel):
    model_config = ConfigDict(
        populate_by_name=True,
        json_schema_extra={
            "example": {
                "inquiryId": 1001,
                "normalizedQuestion": "로그인 오류 비밀번호를 변경했는데도 로그인 실패가 반복됩니다",
                "predictedCategory": "계정/로그인 문의",
                "categoryScore": 0.95,
                "references": [
                    {
                        "sourceType": "FAQ",
                        "sourceId": 1,
                        "title": "비밀번호를 변경했는데 로그인에 실패합니다.",
                        "score": 0.92,
                    }
                ],
                "draftAnswer": "비밀번호 변경 후 로그인 오류에 대한 안내 초안입니다.",
                "status": "SUCCESS",
            }
        },
    )

    inquiry_id: int = Field(..., alias="inquiryId", description="Inquiry ID")
    normalized_question: str = Field(..., alias="normalizedQuestion", description="Normalized inquiry text")
    predicted_category: str = Field(..., alias="predictedCategory", description="Predicted category")
    category_score: float = Field(..., alias="categoryScore", ge=0.0, le=1.0, description="Category confidence score")
    references: List[ReferenceItem] = Field(default_factory=list, description="Reference document list")
    draft_answer: Optional[str] = Field(default=None, alias="draftAnswer", description="Generated draft answer")
    status: Literal["SUCCESS", "PARTIAL_SUCCESS", "FAIL"] = Field(..., description="Processing status")
