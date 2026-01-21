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

import io.github.woong.graphql.server.execution.GraphQLRequestHandler
import io.github.woong.graphql.server.execution.GraphQLServer
import org.springframework.web.servlet.function.ServerRequest

/**
 * Server object that requires the other Spring specific server implementations.
 */
open class SpringGraphQLServer(
    requestParser: SpringGraphQLRequestParser,
    contextFactory: SpringGraphQLContextFactory,
    requestHandler: GraphQLRequestHandler,
) : GraphQLServer<ServerRequest>(requestParser, contextFactory, requestHandler)
