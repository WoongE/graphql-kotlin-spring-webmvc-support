# GraphQL Kotlin Spring WebMVC

A Spring WebMVC (non-reactive) alternative to [graphql-kotlin-spring-server](https://github.com/ExpediaGroup/graphql-kotlin).

## Why?

The official [graphql-kotlin](https://github.com/ExpediaGroup/graphql-kotlin) library only supports **Spring WebFlux (reactive)** for Spring servers. However, many Spring applications use **Spring WebMVC (servlet-based)**, and with Java 21+ Virtual Threads, WebMVC is becoming increasingly attractive again.

This library provides WebMVC support by removing the `suspend` functions from the core interfaces and using `CompletableFuture.join()` instead of coroutine-based execution.

## Modules

- **graphql-kotlin-webmvc**: Core module with non-suspend interfaces
- **graphql-kotlin-spring-webmvc**: Spring Boot autoconfiguration for WebMVC

## Installation

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.github.woong:graphql-kotlin-spring-webmvc:0.1.0-SNAPSHOT")
}
```

## Quick Start

1. Add the dependency
2. Configure your packages in `application.yml`:

```yaml
graphql:
  packages:
    - com.example.graphql
```

3. Create Query/Mutation classes:

```kotlin
import io.github.woong.graphql.server.operations.Query
import org.springframework.stereotype.Component

@Component
class HelloQuery : Query {
    fun hello(name: String): String = "Hello, $name!"
}
```

4. That's it! Your GraphQL endpoint is available at `/graphql`

## Virtual Threads

For best performance with Java 21+, enable Virtual Threads:

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

## Key Differences from graphql-kotlin-spring-server

| Aspect | graphql-kotlin-spring-server | graphql-kotlin-spring-webmvc |
|--------|------------------------------|------------------------------|
| Web Stack | WebFlux (reactive) | WebMVC (servlet) |
| Async Model | Coroutines (`suspend`) | CompletableFuture (blocking) |
| Request Type | `o.s.web.reactive.function.server.ServerRequest` | `o.s.web.servlet.function.ServerRequest` |
| Router DSL | `coRouter { }` | `router { }` |

## Current Status

- [x] Basic HTTP GraphQL endpoint (Query/Mutation)
- [x] Batching support
- [x] DataLoader support
- [x] Automatic Persisted Queries
- [ ] WebSocket Subscriptions (coming soon)
- [ ] GraphiQL/Playground endpoints

## License

Apache License 2.0

## Credits

Based on [ExpediaGroup/graphql-kotlin](https://github.com/ExpediaGroup/graphql-kotlin) (Apache License 2.0)
