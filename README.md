# RAG Kumoh Assistant

금오공과대학교 학사 민원 처리를 위한 **RAG 기반 AI 답변 지원 시스템**입니다.

학생이 문의를 등록하면 관리자는 AI가 생성한 답변 초안과 과거 유사 민원 답변을 참고하여 최종 답변을 작성할 수 있습니다. 최종 답변은 관리자의 승인 후 학생에게 제공되며, 이후 반복 민원 추천 데이터로 활용됩니다.

---

## 프로젝트 개요

대학 행정 민원에서는 수강신청, 등록금, 장학금, 휴복학, 졸업 등 비슷한 문의가 반복적으로 발생합니다.

본 프로젝트는 학교 공지사항, QnA, 과거 최종 답변 이력을 기반으로 관련 자료를 검색하고, Gemini API를 활용해 답변 초안을 생성하는 시스템입니다.

AI가 바로 학생에게 답변하는 구조가 아니라, **관리자가 AI 초안을 검토하고 최종 승인하는 반자동 상담 지원 방식**입니다.

---

## 주요 기능

### 학생 기능

* 회원가입 및 로그인
* 문의 등록
* 본인 문의 목록 조회
* 최종 답변 확인

### 관리자 기능

* 전체 문의 조회
* AI 답변 초안 생성
* 유사 민원 답변 추천
* 최종 답변 작성 및 승인

### AI 기능

* 문의 내용 전처리
* 도메인 용어 오타 보정
* ChromaDB 기반 유사 문서 검색
* Gemini API 기반 답변 초안 생성
* 과거 최종 답변 HISTORY 저장 및 재활용

---

## 기술 스택

| 구분        | 기술                                     |
| --------- | -------------------------------------- |
| Frontend  | React, JavaScript, CSS                 |
| Backend   | Spring Boot, Spring Security, JWT, JPA |
| Database  | MySQL                                  |
| AI Server | FastAPI, Python                        |
| Vector DB | ChromaDB                               |
| Embedding | BGE-M3, sentence-transformers          |
| LLM       | Gemini API                             |
| Crawling  | Jsoup                                  |

---

## 프로젝트 구조

```text
rag-kumoh-assistant
├─ frontend          # React 프론트엔드
├─ backend           # Spring Boot 백엔드
├─ ai-draft-server   # FastAPI AI 서버
├─ .gitignore
└─ README.md
```

---

## 시스템 흐름

```text
학생 문의 등록
↓
Spring Boot 서버에서 문의 저장
↓
관리자가 AI 초안 생성 요청
↓
FastAPI AI 서버 호출
↓
ChromaDB에서 관련 문서 검색
↓
Gemini API로 답변 초안 생성
↓
관리자가 검토 후 최종 답변 승인
↓
최종 답변을 HISTORY 데이터로 저장
↓
이후 유사 민원 추천에 활용
```

---

## AI 서버 의존성 설치

AI 서버는 `requirements.txt`를 통해 Python 패키지를 관리합니다.

```bash
cd ai-draft-server

python -m venv .venv
```

Windows:

```bash
.\.venv\Scripts\activate
```

macOS/Linux:

```bash
source .venv/bin/activate
```

의존성 설치:

```bash
pip install -r requirements.txt
```

---

## 실행 방법

### 1. 저장소 clone

```bash
git clone https://github.com/wfefd/rag-kumoh-assistant.git
cd rag-kumoh-assistant
```

---

### 2. MySQL 데이터베이스 생성

```sql
CREATE DATABASE civil_ai
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

---

### 3. AI 서버 실행

```bash
cd ai-draft-server
uvicorn main:app --reload --port 8000
```

AI 서버 주소:

```text
http://localhost:8000
```

FastAPI 문서:

```text
http://localhost:8000/docs
```

---

### 4. 백엔드 실행

```bash
cd backend
./gradlew bootRun
```

Windows:

```bash
.\gradlew bootRun
```

백엔드 서버 주소:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

---

### 5. 프론트엔드 실행

```bash
cd frontend
npm install
npm run dev
```

프론트엔드 주소:

```text
http://localhost:5173
```

---

## 주요 API

| 기능       | Method | URL                                            |
| -------- | ------ | ---------------------------------------------- |
| 회원가입     | POST   | `/api/auth/signup`                             |
| 로그인      | POST   | `/api/auth/login`                              |
| 문의 등록    | POST   | `/api/inquiries`                               |
| 내 문의 조회  | GET    | `/api/inquiries/my`                            |
| 전체 문의 조회 | GET    | `/api/inquiries`                               |
| AI 초안 생성 | POST   | `/api/inquiries/{inquiryId}/ai-recommendation` |
| 유사 답변 추천 | GET    | `/api/inquiries/{inquiryId}/similar-answers`   |
| 최종 답변 승인 | POST   | `/api/inquiries/{inquiryId}/answers/approve`   |

---

## RAG 구조

AI 답변 초안 생성은 다음 데이터를 기반으로 수행됩니다.

| 데이터     | 설명                | 활용         |
| ------- | ----------------- | ---------- |
| NOTICE  | 학교 공지사항           | 답변 초안 참고자료 |
| QNA     | 학교 QnA 데이터        | 답변 초안 참고자료 |
| HISTORY | 관리자가 승인한 과거 최종 답변 | 반복 민원 추천   |

AI 초안 생성은 `NOTICE`, `QNA`, `HISTORY`를 참고하고, 반복 민원 추천은 `HISTORY` 데이터만 검색합니다.

---

## 주의사항

아래 정보는 GitHub에 올리지 않아야 합니다.

```text
.env
Gemini API Key
DB password
JWT secret
QnA login cookie
chroma_db/
node_modules/
.venv/
```

환경변수 또는 로컬 설정 파일로 관리해야 합니다.

---

## 프로젝트 의의

본 프로젝트는 단순 문의 게시판이 아니라, 학교 행정 데이터를 검색 가능한 지식 기반으로 만들고 이를 AI 답변 생성에 활용하는 시스템입니다.

AI가 최종 답변을 자동으로 확정하지 않고, 관리자가 검토 후 승인하는 구조를 통해 행정 답변의 신뢰성과 책임성을 유지하는 것을 목표로 합니다.
