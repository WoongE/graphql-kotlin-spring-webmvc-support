# CLAUDE.md

GraphQL Kotlin Spring WebMVC Support 프로젝트 가이드

## 프로젝트 개요

Spring WebMVC(서블릿 기반)를 위한 GraphQL Kotlin 라이브러리.
[graphql-kotlin](https://github.com/ExpediaGroup/graphql-kotlin)이 WebFlux만 지원하는 문제를 해결.

**레포지토리:** https://github.com/WoongE/graphql-kotlin-spring-webmvc-support

## 빌드 명령어

```bash
# 전체 빌드
./gradlew build

# 테스트만
./gradlew test

# 린트 체크
./gradlew ktlintCheck

# 린트 자동 수정
./gradlew ktlintFormat

# 특정 모듈 빌드
./gradlew :graphql-kotlin-webmvc:build
./gradlew :graphql-kotlin-spring-webmvc:build
```

## 모듈 구조

```
graphql-kotlin-spring-webmvc-support/
├── graphql-kotlin-webmvc/             # 핵심 모듈 (suspend 제거 버전)
│   └── io.github.woong.graphql.server
│       ├── execution/                 # GraphQLServer, Handler, Parser, ContextFactory
│       ├── extensions/                # 확장 함수
│       ├── operations/                # Query, Mutation, Subscription 마커
│       └── types/                     # Request, Response 타입
│
└── graphql-kotlin-spring-webmvc/      # Spring Boot 자동설정
    └── io.github.woong.graphql.spring
        ├── execution/                 # Spring 전용 구현체
        ├── extensions/                # Generator 확장
        └── *Configuration.kt          # 자동설정
```

## 핵심 설계 결정

### suspend 제거

원본 graphql-kotlin-server는 모든 인터페이스가 `suspend` 함수.
WebMVC에서 사용하기 위해 suspend를 제거하고 `CompletableFuture.join()` 사용.

```kotlin
// 원본 (WebFlux)
suspend fun execute(request: Request): GraphQLServerResponse?

// 이 프로젝트 (WebMVC)
fun execute(request: Request): GraphQLServerResponse?
```

### ServerRequest 차이

- WebFlux: `org.springframework.web.reactive.function.server.ServerRequest`
- WebMVC: `org.springframework.web.servlet.function.ServerRequest`

### Router DSL 차이

- WebFlux: `coRouter { }` (코루틴)
- WebMVC: `router { }` (일반)

## 의존성

graphql-kotlin 공식 라이브러리를 의존성으로 사용 (server 모듈 제외):

- `graphql-kotlin-schema-generator`
- `graphql-kotlin-federation`
- `graphql-kotlin-dataloader-instrumentation`
- `graphql-kotlin-automatic-persisted-queries`

## 남은 작업

### 우선순위 높음

- [ ] WebSocket Subscription 지원
    - GraphQLWebSocketServer suspend 제거 버전 구현
    - Spring WebSocket Handler 구현
    - 구독 자동설정

### 우선순위 중간

- [ ] GraphiQL 엔드포인트
- [ ] Playground 엔드포인트
- [ ] SDL 엔드포인트

### 우선순위 낮음

- [ ] 테스트 작성
- [ ] 예제 프로젝트
- [ ] Maven Central 배포 설정
- [ ] 문서화

## 참고 자료

- 원본 graphql-kotlin: `/Users/woonge/japan/graphql-kotlin`
- 회사 프로젝트 참고 코드:
  `/Users/woonge/japan/doctornow-jp-backend/bff/src/main/kotlin/kr/doctornow/gateway/configuration/graphql`
