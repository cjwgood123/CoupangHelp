# 쿠팡 판매자 데이터 분석

Spring Boot + Thymeleaf 기반의 쿠팡 판매자 데이터 분석 웹 애플리케이션입니다.

## 기술 스택

- **Backend**: Java 17, Spring Boot 3.2.0
- **Template Engine**: Thymeleaf
- **Build Tool**: Gradle
- **Frontend**: Tailwind CSS
- **Styling**: Custom CSS with Tailwind

## 프로젝트 구조

```
cou_html/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/coupang/
│   │   │       ├── CouHtmlApplication.java
│   │   │       └── controller/
│   │   │           └── IndexController.java
│   │   └── resources/
│   │       ├── static/
│   │       │   └── css/
│   │       │       └── index.css
│   │       ├── templates/
│   │       │   ├── index.html
│   │       │   ├── board.html
│   │       │   └── fragments/
│   │       │       └── sidebar.html
│   │       └── application.properties
│   └── test/
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## 실행 방법

### 1. Gradle 빌드 및 실행

```bash
# Windows
gradlew.bat bootRun

# Linux/Mac
./gradlew bootRun
```

### 2. IDE에서 실행

- IntelliJ IDEA나 Eclipse에서 `CouHtmlApplication.java`를 실행

### 3. 빌드된 JAR 실행

```bash
gradlew build
java -jar build/libs/cou-html-0.0.1-SNAPSHOT.jar
```

## Tailwind CSS 빌드

Tailwind CSS를 사용하기 위해 CSS 파일을 빌드해야 합니다:

```bash
# npm이 설치되어 있어야 합니다
npm install
npm run build
```

또는 개발 모드로 실행:

```bash
npm run dev
```

빌드된 CSS 파일은 `src/main/resources/static/css/index.css`에 생성됩니다.

## 주요 기능

- 메인 페이지: 통계 카드 및 빠른 링크 제공
- 사이드바: 월별/카테고리별 네비게이션
- 상품 목록 페이지: 선택한 조건에 따른 상품 목록 표시

## 포트

기본 포트: `8080`

애플리케이션 실행 후 `http://localhost:8080`에서 접속할 수 있습니다.

## 개발 환경

- Java 17 이상
- Gradle 7.x 이상
- Node.js 16.x 이상 (Tailwind CSS 빌드용)
