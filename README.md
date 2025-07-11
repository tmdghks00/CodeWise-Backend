**CodeWise-Backend
AI 기반 실시간 코드 분석 시스템의 백엔드 저장소입니다.**

🛠️ 사용된 기술 및 개발 환경
언어: Java 17

프레임워크: Spring Boot

빌드 도구: Gradle

데이터베이스: MySQL 8.0

ORM: Spring Data JPA, Hibernate

인증/권한: Spring Security, JWT, Spring Security OAuth2 Client

실시간 통신: Spring WebSocket (STOMP)

API 문서: Swagger UI (springdoc-openapi)

외부 연동: Python 기반 AI 분석 서버 (HTTP 통신), Google OAuth2 API

IDE: IntelliJ IDEA


🚀 CodeWise 백엔드 API 엔드포인트

1. 인증 및 사용자 관리 (Auth & User)
인증 POST /auth/signup - 새로운 사용자 계정을 생성합니다.

인증 POST /auth/login - 이메일과 비밀번호로 로그인하고 JWT 토큰을 발급받습니다.

사용자 GET /user/me - 현재 로그인된 사용자의 정보를 조회합니다.

사용자 DELETE /user/me - 현재 로그인된 사용자 계정을 삭제합니다.

사용자 PUT /user/me - 현재 로그인된 사용자의 정보를 수정합니다. (비밀번호 또는 이메일 선택 수정)

OAuth2 GET /oauth2/authorization/google - Google OAuth2 로그인 페이지로 리다이렉트합니다.

OAuth2 GET /oauth2/callback/google - Google 로그인 성공 후 콜백을 처리하고 JWT 토큰을 포함하여 프론트엔드로 리다이렉트합니다. (프론트엔드 리다이렉트 URL: http://localhost:3000/oauth2/redirect?token=발급된_JWT_토큰)

2. 코드 제출 (Code Submission)
코드 제출 POST /code/submit - 사용자가 코드를 제출하고 저장합니다.

코드 제출 GET /code/list - 현재 로그인된 사용자가 제출한 모든 코드 목록을 조회합니다.

코드 제출 GET /code/{id} - 특정 ID를 가진 제출된 코드의 상세 내용을 조회합니다.

코드 제출 DELETE /code/{id} - 특정 ID를 가진 제출된 코드를 삭제합니다.

코드 제출 GET /code/submission/user - 현재 로그인한 사용자의 모든 코드 제출 내역을 조회합니다.

3. 분석 결과 (Analysis Result)
분석 결과 GET /analysis/result/{submissionId} - 특정 코드 제출 ID에 대한 AI 분석 결과를 조회합니다.

분석 결과 GET /analysis/user/{username} - (관리자 또는 특정 사용자 이력을 볼 때 사용) 특정 사용자의 모든 분석 결과를 조회합니다.

분석 결과 GET /analysis/history - 현재 로그인된 사용자의 모든 분석 이력을 조회합니다.

분석 결과 GET /analysis/{id} - 특정 분석 결과 ID에 대한 상세 내용을 조회합니다.

분석 결과 GET /user/history - 현재 로그인된 사용자의 분석 이력을 정렬 및 필터링하여 조회합니다.

쿼리 파라미터: sortBy (id, score, maintainability, readability, bug 중 택 1), direction (asc, desc 중 택 1), keyword (선택, 요약 내용 키워드)

4. 실시간 코드 분석 (WebSocket)
WebSocket CONNECT /ws - STOMP 프로토콜을 사용하여 WebSocket 연결을 설정합니다.

WebSocket SEND /app/analyze - 클라이언트에서 작성 중인 코드를 실시간으로 분석 요청합니다.

WebSocket SUBSCRIBE /topic/result - 실시간 분석 결과를 받기 위해 구독합니다.

5. 기타 (Other)
상태 확인 GET / - 백엔드 서버의 실행 여부를 확인합니다
