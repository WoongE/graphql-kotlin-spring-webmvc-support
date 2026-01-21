# GraphQL-Kotlin WebMVC Support 개발 계획

## 프로젝트 현황

**레포지토리:** https://github.com/WoongE/graphql-kotlin-spring-webmvc-support

### 완료된 작업 ✅

#### Phase 1: 프로젝트 초기 설정

- [x] 새 레포지토리 생성 및 Gradle 멀티모듈 설정
- [x] 빌드 설정 (libs.versions.toml, build.gradle.kts)
- [x] 라이선스, README 작성
- [x] .gitignore 설정

#### Phase 2: graphql-kotlin-webmvc 모듈 (핵심)

- [x] 타입 모델 (GraphQLRequest, GraphQLResponse 등)
- [x] 핵심 인터페이스 (GraphQLServer, GraphQLRequestHandler 등)
- [x] 확장 함수 (RequestExtensions, ResponseExtensions)
- [x] 마커 인터페이스 (Query, Mutation, Subscription)

#### Phase 3: graphql-kotlin-spring-webmvc 모듈 (Spring 통합)

- [x] SpringGraphQLServer, SpringGraphQLRequestParser, SpringGraphQLContextFactory
- [x] GraphQLRoutesConfiguration (router DSL)
- [x] 자동설정 클래스들 (GraphQLAutoConfiguration, GraphQLSchemaConfiguration 등)
- [x] SpringDataFetcher, SpringKotlinDataFetcherFactoryProvider

---

## 남은 작업 📋

### Phase 2 추가: Subscription 지원 (핵심 모듈)

- [ ] `GraphQLWebSocketServer` (suspend 제거 버전)
- [ ] `GraphQLSubscriptionRequestParser`
- [ ] `GraphQLSubscriptionContextFactory`
- [ ] `GraphQLSubscriptionHooks`
- [ ] Subscription 관련 타입들

### Phase 3 추가: Subscription 자동설정 (Spring 모듈)

- [ ] `SubscriptionWebSocketHandler` (Spring WebSocket)
- [ ] `SpringGraphQLSubscriptionRequestParser`
- [ ] `SpringSubscriptionGraphQLContextFactory`
- [ ] `SubscriptionAutoConfiguration`
- [ ] `WebSocketConfigurer` 구현

### Phase 4: 추가 기능

- [ ] GraphiQL 엔드포인트
- [ ] Playground 엔드포인트
- [ ] SDL 엔드포인트

### Phase 5: 배포 준비

- [ ] 테스트 작성
- [ ] 예제 프로젝트
- [ ] Maven Central 배포 설정
- [ ] 문서화
- [ ] 커뮤니티 홍보

---

## 기술적 배경

### 왜 이 프로젝트가 필요한가?

1. **graphql-kotlin은 WebFlux만 지원**
    - Spring 사용자 중 WebMVC 사용자가 훨씬 많음
    - Java 21+ Virtual Thread로 WebMVC가 다시 주목받는 중

2. **핵심 문제: suspend 함수**
    - graphql-kotlin-server의 모든 인터페이스가 `suspend`
    - WebMVC Controller에서 직접 호출 불가

3. **해결책: suspend 제거**
    - `graphQL.executeAsync().await()` → `graphQL.executeAsync().join()`
    - Virtual Thread 환경에서 blocking이어도 문제 없음

### 주요 변경점

| 항목            | WebFlux (원본)                       | WebMVC (이 프로젝트)            |
|---------------|------------------------------------|----------------------------|
| suspend       | `suspend fun`                      | `fun`                      |
| 비동기           | `executeAsync().await()`           | `executeAsync().join()`    |
| ServerRequest | `o.s.web.reactive.function.server` | `o.s.web.servlet.function` |
| Router DSL    | `coRouter { }`                     | `router { }`               |

### 의존성 구조

```
graphql-kotlin 공식 라이브러리 (의존성으로 사용)
├── graphql-kotlin-schema-generator
├── graphql-kotlin-federation
├── graphql-kotlin-dataloader-instrumentation
└── graphql-kotlin-automatic-persisted-queries

이 프로젝트 (suspend 제거 버전 직접 구현)
├── graphql-kotlin-webmvc (핵심)
└── graphql-kotlin-spring-webmvc (Spring 통합)
```

---

## 참고 자료

### 원본 코드 위치

- graphql-kotlin 원본: `/Users/woonge/japan/graphql-kotlin`
    - `servers/graphql-kotlin-server` - 핵심 모듈
    - `servers/graphql-kotlin-spring-server` - Spring WebFlux 통합

### 회사 프로젝트 참고 코드

- `/Users/woonge/japan/doctornow-jp-backend/bff/src/main/kotlin/kr/doctornow/gateway/configuration/graphql`
    - `server/` - suspend 제거된 핵심 모듈
    - `spring/` - WebMVC Spring 통합

### Subscription 구현 참고

원본 graphql-kotlin-server의 subscription 관련 파일:

- `execution/subscription/GraphQLWebSocketServer.kt`
- `execution/subscription/GraphQLSubscriptionRequestParser.kt`
- `execution/subscription/GraphQLSubscriptionContextFactory.kt`
- `execution/subscription/GraphQLSubscriptionHooks.kt`
- `types/GraphQLSubscriptionMessage.kt`
- `types/GraphQLSubscriptionStatus.kt`

원본 graphql-kotlin-spring-server의 subscription 관련 파일:

- `subscriptions/SubscriptionWebSocketHandler.kt`
- `subscriptions/SpringGraphQLSubscriptionRequestParser.kt`
- `subscriptions/SpringSubscriptionGraphQLContextFactory.kt`
- `SubscriptionAutoConfiguration.kt`
