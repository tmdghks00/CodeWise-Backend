# 🚀 CodeWise 백엔드

**AI 기반 실시간 코드 분석 시스템의 백엔드 저장소**

---

## 🛠️ 기술 스택 & 개발 환경

- **언어**: Java 17  
- **프레임워크**: Spring Boot  
- **빌드 도구**: Gradle  
- **IDE**: IntelliJ IDEA  
- **데이터베이스**: MySQL 8.0  
- **ORM**: Spring Data JPA, Hibernate  
- **인증/인가**: Spring Security, JWT, OAuth2 Client  
- **실시간 통신**: Spring WebSocket (STOMP)  
- **API 문서화**: Swagger UI (springdoc-openapi)  
- **외부 연동**  
  - Python 기반 AI 분석 서버 (HTTP 통신)  
  - Google OAuth2 API

---

## 📌 API 엔드포인트 요약

### 1. 🔐 인증 및 사용자 관리 (Auth & User)

- `POST /auth/signup`  
  → 새로운 사용자 계정 생성

- `POST /auth/login`  
  → 이메일 + 비밀번호로 로그인 & JWT 토큰 발급

- `GET /user/me`  
  → 현재 로그인된 사용자 정보 조회

- `PUT /user/me`  
  → 사용자 정보 수정 (비밀번호 또는 이메일)

- `DELETE /user/me`  
  → 사용자 계정 삭제

**OAuth2 로그인**

- `GET /oauth2/authorization/google`  
  → Google OAuth2 로그인 리다이렉트

- `GET /oauth2/callback/google`  
  → 로그인 성공 후 JWT 포함하여 프론트엔드로 리다이렉트  
  - 예시:  
    `http://localhost:3000/oauth2/redirect?token=발급된_JWT_토큰`

---

### 2. 📥 코드 제출 (Code Submission)

- `POST /code/submit`  
  → 코드 제출 및 저장

- `GET /code/list`  
  → 제출한 코드 목록 전체 조회

- `GET /code/{id}`  
  → 특정 코드 ID 조회

- `DELETE /code/{id}`  
  → 특정 코드 삭제

- `GET /code/submission/user`  
  → 로그인된 사용자 코드 제출 내역 조회 (이 엔드포인트를 통해 모든 사용자 코드 제출 내역을 조회)

---

### 3. 📊 분석 결과 (Analysis Result)

- `GET /analysis/result/{submissionId}`  
  → 특정 제출 ID에 대한 분석 결과 조회

- `GET /analysis/user/{username}`  
  → 특정 사용자 분석 결과 전체 조회 (관리자용)

- `GET /analysis/history`  
  → 로그인된 사용자 분석 이력 조회

- `GET /analysis/{id}`  
  → 특정 분석 결과 상세 조회

- `GET /user/history`  
  → 사용자 분석 이력 정렬/필터 조회 (이 엔드포인트를 통해 로그인된 사용자의 모든 분석 이력을 정렬 및 필터링하여 조회)

  **쿼리 파라미터**  
  - `sortBy`: `id`, `score`, `maintainability`, `readability`, `bug`  
  - `direction`: `asc` or `desc`  
  - `keyword`: (선택) 요약 키워드

---

### 4. ⚡ 실시간 코드 분석 (WebSocket)

- `CONNECT /ws`  
  → WebSocket 연결 (STOMP 프로토콜 사용)

- `SEND /app/analyze`  
  → 실시간 분석 요청 전송

- `SUBSCRIBE /user/queue/result`  
  → 특정 사용자에게만 전송되는 실시간 분석 결과 수신 (각 사용자의 고유 큐로 결과가 전송)

---

### 5. ✅ 기타 (Other)

- `GET /`  
  → 백엔드 서버 상태 확인

---
