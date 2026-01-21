plugins {
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.kapt)
}

description = "Spring Boot autoconfiguration for GraphQL WebMVC server"

dependencies {
    api(projects.graphqlKotlinWebmvc)
    api(libs.graphql.kotlin.federation)
    api(libs.spring.boot.web)
    api(libs.spring.boot.websocket)
    api(libs.spring.context)

    kapt(libs.spring.boot.config)

    testImplementation(libs.kotlin.junit.test)
    testImplementation(libs.mockk)
    testImplementation(libs.spring.boot.test)
}
