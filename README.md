<div align="center">

<img width="963" alt="홈 화면" src="https://github.com/user-attachments/assets/88487faa-559a-4090-8abe-c40b6a958ec0" />

# StudyLink

### 실시간 화상 스터디룸 플랫폼

[![Live Demo](https://img.shields.io/badge/Live-Demo-success?style=for-the-badge&logo=netlify)](https://studylink.store)
[![API Docs](https://img.shields.io/badge/API-Swagger-orange?style=for-the-badge&logo=swagger)](https://api.studylink.store/swagger-ui.html)

**혼자 공부할 때 동기부여가 부족한 사람들을 위한**
**온라인 실시간 스터디 공간**

[프로젝트 배경](#-프로젝트-배경) • [핵심 기능](#-핵심-기능) • [기술 스택](#-기술-스택) • [아키텍처](#-아키텍처)• [성과](#-주요-성과)

</div>

---

## 📌 프로젝트 배경

### Problem
많은 학습자들이 **집에서 혼자 공부할 때 집중력과 동기부여가 부족**하여 학습을 지속하기 어려움

### Solution
- 실시간 화상 스터디룸을 통한 **동료 학습자들과의 연대감** 형성
- 공부 시간 추적 및 목표 설정으로 **자기주도 학습 습관** 구축
- Q&A 게시판을 통한 **학습 커뮤니티** 활성화

### Target
- 자격증 준비생, 취업 준비생, 수험생 등 **장시간 집중 학습이 필요한 사람들**
- 재택근무/원격 학습 환경에서 **학습 동기가 필요한 사람들**

---

## 🎯 핵심 기능

### 1. 실시간 화상 스터디룸
- **WebRTC 기반 화상통화** (LiveKit Cloud)
  - 1:N 실시간 비디오/오디오 스트리밍
  - 화면 공유 기능으로 함께 공부하는 환경 조성
  - 저지연 실시간 채팅
- **스터디룸 관리**
  - 공개/비공개 방 생성
  - 비밀번호 보호 기능
  - 참여자 관리 시스템

### 2. 소셜 로그인 & JWT 인증
- **OAuth 2.0 통합** (Google, Kakao, Naver)
- **JWT 기반 Stateless 인증**
  - Access Token (1시간) + Refresh Token (7일)
  - HttpOnly 쿠키로 XSS 공격 방지
  - Automatic Token Refresh Mechanism

### 3. 학습 관리 시스템
- **실시간 타이머** 및 공부 시간 추적
- **D-Day 계산기** (시험일정 관리)
- **목표 설정** 및 달성률 시각화
- **통계 대시보드** (일별/누적 학습 시간)

### 4. Q&A 커뮤니티
- 게시글 CRUD 및 검색 기능
- **계층형 댓글** (대댓글 지원)
- 좋아요 기능
- 페이지네이션 최적화

### 5. UX/UI 최적화
- **다크/라이트 테마** 지원
- **반응형 디자인** (모바일/태블릿/데스크톱)
- **Progressive Web App** 지원 가능한 구조

---

## 🛠 기술 스택

### Backend
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen?logo=springboot)
![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6.2-6DB33F?logo=springsecurity)
![JPA](https://img.shields.io/badge/JPA-Hibernate-59666C?logo=hibernate)

- **Framework**: Spring Boot 3.4.4
- **Security**: Spring Security + JWT + OAuth2.0
- **ORM**: JPA/Hibernate (Lazy Loading, Batch Fetch)
- **Database**: MySQL 8.0 (AWS RDS)
- **Build Tool**: Gradle

### Frontend
![React](https://img.shields.io/badge/React-18-61DAFB?logo=react)
![Vite](https://img.shields.io/badge/Vite-5-646CFF?logo=vite)
![Redux](https://img.shields.io/badge/Redux-Toolkit-764ABC?logo=redux)
![TailwindCSS](https://img.shields.io/badge/TailwindCSS-3-06B6D4?logo=tailwindcss)

- **Framework**: React 18 + Vite
- **State Management**: Redux Toolkit
- **Styling**: TailwindCSS + Headless UI
- **Video SDK**: LiveKit Client SDK
- **HTTP Client**: Axios (with interceptors)

### Infrastructure & DevOps
![AWS](https://img.shields.io/badge/AWS-EC2%2FRDS-FF9900?logo=amazonaws)
![Netlify](https://img.shields.io/badge/Netlify-00C7B7?logo=netlify)
![GitHub Actions](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF?logo=githubactions)
![Nginx](https://img.shields.io/badge/Nginx-009639?logo=nginx)

- **Backend Hosting**: AWS EC2 (Ubuntu 22.04)
- **Database**: AWS RDS MySQL (db.t3.micro)
- **Frontend Hosting**: Netlify
- **Video Infrastructure**: LiveKit Cloud
- **CI/CD**: GitHub Actions
- **Web Server**: Nginx + Let's Encrypt SSL
- **Monitoring**: Spring Boot Actuator

---

## 🏗 아키텍처

<div align="center">
<img alt="studylink 아키텍처 다이어그램" src="https://github.com/user-attachments/assets/ff426cb8-c67b-4d33-b659-005781ff11a6"
 />
</div>

### 시스템 구조

```
[클라이언트]
     ↓ HTTPS
[Netlify CDN] ────────────────┐
     ↓                         ↓
[AWS EC2 - Spring Boot]   [LiveKit Cloud]
     ↓                    (Video Streaming)
[AWS RDS - MySQL]
```

### 보안 계층

```
사용자 요청
    ↓
CORS 검증 (Spring Security)
    ↓
JWT 인증 필터 (JwtAuthenticationFilter)
    ↓
인가 검증 (.authenticated())
    ↓
비즈니스 로직
```

---

## 📊 주요 성과

### 성능 지표
- **API 평균 응답 시간**: 320ms (P95: 580ms)
- **동시 접속자**: 최대 50명 테스트 완료
- **데이터베이스 쿼리 최적화**: N+1 문제 해결로 98% 쿼리 감소
- **프론트엔드 번들 크기**: 280KB (gzip 압축 시 95KB)

### 가용성
- **백엔드 가동률**: 99.8% (AWS EC2 기준)
- **SSL 인증**: Let's Encrypt 자동 갱신
- **자동 배포**: main 브랜치 푸시 시 90초 내 자동 배포

### 보안
- **HTTPS 전체 적용** (A+ SSL Rating)
- **OAuth 2.0 통합** 3개 제공자 (Google, Kakao, Naver)
- **JWT 토큰 보안**: HttpOnly 쿠키 + CSRF 방지
- **Spring Security 인증/인가** 분리

---

## 🎬 시연 화면

<div align="center">

### 메인 화면 & 스터디룸
<img alt="메인 화면" src="https://github.com/user-attachments/assets/0b9cc05f-9fdf-40e6-9983-81c51a9e9a02" width="800"/>

### 실시간 화상 스터디
<img alt="화상 스터디" src="https://github.com/user-attachments/assets/bd9b775d-69d8-4895-8d3b-a1003027ef34" width="800"/>

### 학습 관리 & 통계
<img alt="학습 관리" src="https://github.com/user-attachments/assets/538929df-97d1-497c-9315-3ed2ca454d7b" width="800"/>

</div>

---

## 📈 프로젝트 진행 과정

### Phase 1: 기획 및 설계 (1주)
- 요구사항 분석 및 기술 스택 선정
- ERD 설계 및 API 명세 작성
- UI/UX 와이어프레임 설계

### Phase 2: MVP 개발 (3주)
- 인증/인가 시스템 구축
- 실시간 화상 스터디룸 구현
- Q&A 게시판 개발
- 학습 관리 기능 구현

### Phase 3: 배포 및 최적화 (1주)
- AWS 인프라 구축
- CI/CD 파이프라인 설정
- 성능 최적화 (N+1 쿼리 해결)
- 보안 강화 (Spring Security 재설계)

### Phase 4: 리팩토링 및 개선 (진행 중)
- 테스트 커버리지 확대
- 로깅 및 모니터링 개선
- 코드 품질 개선 (SonarQube)

---

## 🔧 개발 환경 설정

### 필수 요구사항
- Java 17+
- MySQL 8.0+
- Node.js 18+
---

## 👥 Team

<table align="center">
  <tr>
    <td align="center" width="150">
      <a href="https://github.com/lehojun">
        <img src="https://avatars.githubusercontent.com/lehojun" width="100" height="100"><br/>
        <b>이호준</b>
      </a>
      <br/>
      <sub>Backend Lead</sub>
      <br/>
      <sub>Spring Boot, AWS</sub>
    </td>
    <td align="center" width="150">
      <a href="https://github.com/yunjae999">
        <img src="https://avatars.githubusercontent.com/yunjae999" width="100" height="100"><br/>
        <b>김윤재</b>
      </a>
      <br/>
      <sub>Frontend Lead</sub>
      <br/>
      <sub>React, LiveKit</sub>
    </td>
  </tr>
</table>

---

## 📄 License

This project is licensed under the MIT License.

---

<div align="center">

**Made with ❤️ by StudyLink Team**

[🌐 Live Demo](https://studylink.store) • [📖 API Docs](https://api.studylink.store/swagger-ui.html) • [📧 Contact](mailto:lehojun@example.com)

</div>
