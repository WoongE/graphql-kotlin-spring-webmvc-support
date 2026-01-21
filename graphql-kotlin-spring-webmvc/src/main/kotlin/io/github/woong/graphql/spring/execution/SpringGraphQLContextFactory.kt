/*
 * Copyright 2025 Expedia, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.woong.graphql.spring.execution

import com.expediagroup.graphql.generator.extensions.toGraphQLContext
import graphql.GraphQLContext
import io.github.woong.graphql.server.execution.GraphQLContextFactory
import org.springframework.web.servlet.function.ServerRequest

/**
 * Wrapper class for specifically handling the Spring [ServerRequest]
 */
abstract class SpringGraphQLContextFactory : GraphQLContextFactory<ServerRequest>

/**
 * Basic implementation of [SpringGraphQLContextFactory] that populates Apollo tracing header.
 */
open class DefaultSpringGraphQLContextFactory : SpringGraphQLContextFactory() {
    override fun generateContext(request: ServerRequest): GraphQLContext =
        mutableMapOf<Any, Any>()
            .also { map ->
                request.headers().firstHeader(FEDERATED_TRACING_HEADER_NAME)?.let { headerValue ->
                    map[FEDERATED_TRACING_HEADER_NAME] = headerValue
                }
            }.toGraphQLContext()

    companion object {
        const val FEDERATED_TRACING_HEADER_NAME = "apollo-federation-include-trace"
    }
}
