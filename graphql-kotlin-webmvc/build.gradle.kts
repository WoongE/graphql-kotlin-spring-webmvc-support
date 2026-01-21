description = "Core GraphQL server implementation for WebMVC (non-reactive, no suspend)"

dependencies {
    api(libs.graphql.kotlin.schema.generator)
    api(libs.graphql.kotlin.dataloader.instrumentation)
    api(libs.graphql.kotlin.automatic.persisted.queries)
    api(libs.graphql.java)
    api(libs.jackson)
    api(libs.fastjson2)

    testImplementation(libs.kotlin.junit.test)
    testImplementation(libs.mockk)
}
