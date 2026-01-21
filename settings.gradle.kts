rootProject.name = "graphql-kotlin-webmvc-parent"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":graphql-kotlin-webmvc")
include(":graphql-kotlin-spring-webmvc")

project(":graphql-kotlin-webmvc").projectDir = file("graphql-kotlin-webmvc")
project(":graphql-kotlin-spring-webmvc").projectDir = file("graphql-kotlin-spring-webmvc")
