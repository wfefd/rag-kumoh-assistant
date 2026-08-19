from fastapi import FastAPI

from app.api.draft import router as draft_router
from app.api.backend_contract import router as backend_contract_router


app = FastAPI(
    title="AI Draft Server",
    description="AI draft generation MVP server",
    version="0.1.0",
)

app.include_router(draft_router)
app.include_router(backend_contract_router)


@app.get("/")
async def root():
    return {"message": "AI Draft Server is running"}