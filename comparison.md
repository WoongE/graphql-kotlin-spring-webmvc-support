# graphql-kotlin-server vs graphql-kotlin-webmvc 비교

원본 `graphql-kotlin-server` (8.8.1)와 이 프로젝트의 `graphql-kotlin-webmvc` 모듈 비교 문서.

## 파일 구조 비교

| 파일 | 원본 (8.8.1) | WebMVC | 상태 |
|------|:------------:|:------:|------|
| **execution/** |
| GraphQLContextFactory.kt | ✅ | ✅ | suspend 제거 |
| GraphQLRequestHandler.kt | ✅ | ✅ | suspend 제거, 구현 방식 변경 |
| GraphQLRequestParser.kt | ✅ | ✅ | suspend 제거 |
| GraphQLServer.kt | ✅ | ✅ | suspend 제거, coroutine 제거 |
| **execution/subscription/** |
| GraphQLSubscriptionContextFactory.kt | ✅ | ❌ | 미구현 |
| GraphQLSubscriptionHooks.kt | ✅ | ❌ | 미구현 |
| GraphQLSubscriptionRequestParser.kt | ✅ | ❌ | 미구현 |
| GraphQLWebSocketServer.kt | ✅ | ❌ | 미구현 |
| **exception/** |
| MissingDataLoaderException.kt | ✅ | ✅ | 동일 |
| **extensions/** |
| dataFetchingEnvironmentExtensions.kt | ✅ | ✅ | 동일 |
| instrumentationExtensions.kt | ✅ | ✅ | 동일 |
| jsonReaderExtensions.kt | ✅ | ❌ | FastJson 전용, 미구현 |
| requestExtensions.kt | ✅ | ✅ | 동일 |
| responseExtensions.kt | ✅ | ✅ | 동일 |
| **operations/** |
| Mutation.kt | ✅ | ✅ | 동일 |
| Query.kt | ✅ | ✅ | 동일 |
| Subscription.kt | ✅ | ✅ | 동일 |
| **types/** |
| GraphQLServerError.kt | ✅ | ✅ | FastJson 어노테이션 제거 |
| GraphQLServerRequest.kt | ✅ | ✅ | FastJson 지원 제거 |
| GraphQLServerResponse.kt | ✅ | ✅ | FastJson 제거, error() 헬퍼 추가 |
| GraphQLSourceLocation.kt | ✅ | ✅ | 동일 |
| GraphQLSubscriptionMessage.kt | ✅ | ❌ | 미구현 |
| GraphQLSubscriptionStatus.kt | ✅ | ❌ | 미구현 |
| **types/serializers/** |
| FastJsonIncludeNonNullProperty.kt | ✅ | ❌ | FastJson 전용, 미구현 |
| **기타** |
| Schema.kt | ✅ | ❌ | 미구현 |

---

## 주요 변경 사항

### 1. suspend 키워드 제거

모든 인터페이스와 클래스에서 `suspend` 키워드를 제거했습니다.

```kotlin
// 원본 (WebFlux)
suspend fun generateContext(request: Request): GraphQLContext

// WebMVC
fun generateContext(request: Request): GraphQLContext
```

### 2. GraphQLServer.kt

원본은 coroutine 기반으로 `CoroutineScope`와 `SupervisorJob`을 관리합니다.
WebMVC 버전은 단순 blocking 호출로 변경했습니다.

```kotlin
// 원본 (WebFlux)
open suspend fun execute(request: Request): GraphQLServerResponse? =
    coroutineScope {
        requestParser.parseRequest(request)?.let { graphQLRequest ->
            val graphQLContext = contextFactory.generateContext(request)
            val customCoroutineContext = (graphQLContext.get<CoroutineContext>() ?: EmptyCoroutineContext)
            val graphQLExecutionScope = CoroutineScope(
                coroutineContext + customCoroutineContext + SupervisorJob()
            )
            val graphQLContextWithCoroutineScope = graphQLContext + mapOf(
                CoroutineScope::class to graphQLExecutionScope
            )
            requestHandler.executeRequest(graphQLRequest, graphQLContextWithCoroutineScope)
        }
    }

// WebMVC
open fun execute(request: Request): GraphQLServerResponse? {
    val graphQLRequest = requestParser.parseRequest(request) ?: return null
    val graphQLContext = contextFactory.generateContext(request)
    return requestHandler.executeRequest(graphQLRequest, graphQLContext)
}
```

### 3. GraphQLRequestHandler.kt

비동기 처리 방식이 다릅니다.

| 항목 | 원본 (WebFlux) | WebMVC |
|------|---------------|--------|
| 단일 실행 | `executeAsync().await()` | `executeAsync().join()` |
| 순차 실행 | `suspend fun` + map | `fun` + map |
| 병렬 실행 | `supervisorScope` + `async/awaitAll` | `CompletableFuture.allOf()` |
| 에러 처리 | try-catch | `runCatching/getOrElse` |
| Subscription | `executeSubscription()` 메서드 있음 | 미구현 |

```kotlin
// 원본 - 병렬 실행
private suspend fun executeConcurrently(...): GraphQLBatchResponse {
    val responses = supervisorScope {
        batchRequest.requests.map { request ->
            async { execute(request, batchGraphQLContext, dataLoaderRegistry) }
        }.awaitAll()
    }
    return GraphQLBatchResponse(responses)
}

// WebMVC - 병렬 실행
private fun executeConcurrently(...): GraphQLBatchResponse {
    val futures = batchRequest.requests
        .map { graphQL.executeAsync(it.toExecutionInput(batchGraphQLContext, dataLoaderRegistry)) }
    val responses = CompletableFuture
        .allOf(*futures.toTypedArray())
        .thenApply {
            futures.map { future ->
                runCatching { future.join().toGraphQLResponse() }
                    .getOrElse { GraphQLResponse.error(it) }
            }
        }.join()
    return GraphQLBatchResponse(responses)
}
```

### 4. FastJson 지원 제거

원본은 Jackson과 FastJson 두 가지 JSON 라이브러리를 지원합니다.
WebMVC 버전은 Jackson만 지원합니다.

제거된 항목:
- `@JSONType` 어노테이션
- `FastJsonIncludeNonNullProperty` 필터
- `FastJsonGraphQLServerRequestDeserializer`
- `jsonReaderExtensions.kt`

### 5. GraphQLResponse.error() 헬퍼 추가

WebMVC 버전에만 있는 편의 메서드입니다.

```kotlin
data class GraphQLResponse<T>(...) : GraphQLServerResponse() {
    companion object {
        fun error(e: Throwable) = GraphQLResponse<Any?>(
            errors = listOf(e.toGraphQLError().toGraphQLKotlinType())
        )
    }
}
```

---

## 미구현 기능

### Subscription 관련 (6개 파일)
- `execution/subscription/GraphQLWebSocketServer.kt`
- `execution/subscription/GraphQLSubscriptionContextFactory.kt`
- `execution/subscription/GraphQLSubscriptionHooks.kt`
- `execution/subscription/GraphQLSubscriptionRequestParser.kt`
- `types/GraphQLSubscriptionMessage.kt`
- `types/GraphQLSubscriptionStatus.kt`

### FastJson 관련 (2개 파일)
- `extensions/jsonReaderExtensions.kt`
- `types/serializers/FastJsonIncludeNonNullProperty.kt`

### 기타
- `Schema.kt` - 스키마 유틸리티

---

## 의도적으로 제외한 기능

| 기능 | 이유 |
|------|------|
| FastJson 지원 | Spring WebMVC는 기본적으로 Jackson 사용, 복잡도 감소 |
| Coroutine 컨텍스트 관리 | WebMVC는 blocking 방식이므로 불필요 |

---

## 다음 단계

1. **Subscription 지원 구현** - WebSocket 기반 subscription
2. **테스트 작성** - 단위 테스트 및 통합 테스트
3. **Schema.kt 추가 검토** - 필요성 평가 후 결정
