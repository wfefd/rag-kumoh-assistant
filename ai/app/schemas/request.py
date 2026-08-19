from pydantic import BaseModel, ConfigDict, Field


class DraftRequest(BaseModel):
    model_config = ConfigDict(
        populate_by_name=True,
        json_schema_extra={
            "example": {
                "inquiryId": 1001,
                "title": "로그인 오류",
                "content": "비밀번호를 변경했는데도 로그인 실패가 반복됩니다.",
            }
        },
    )

    inquiry_id: int = Field(..., alias="inquiryId", description="Inquiry ID")
    title: str = Field(..., min_length=1, max_length=200, description="Inquiry title")
    content: str = Field(..., min_length=1, max_length=5000, description="Inquiry body")
