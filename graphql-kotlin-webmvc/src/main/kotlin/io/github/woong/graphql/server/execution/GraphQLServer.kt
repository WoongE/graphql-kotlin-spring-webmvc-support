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

package io.github.woong.graphql.server.execution

import io.github.woong.graphql.server.types.GraphQLServerResponse

/**
 * A basic server implementation that parses the incoming request and returns a [GraphQLServerResponse].
 * Subscriptions require more server-specific details and should be implemented separately.
 */
open class GraphQLServer<Request>(
    private val requestParser: GraphQLRequestParser<Request>,
    private val contextFactory: GraphQLContextFactory<Request>,
    private val requestHandler: GraphQLRequestHandler,
) {
    /**
     * Default execution logic for handling a [Request] and returning a [GraphQLServerResponse].
     *
     * If null is returned, that indicates a problem parsing the request or context.
     * If the request is valid, a [GraphQLServerResponse] should always be returned.
     * In the case of errors or exceptions, return a response with GraphQLErrors populated.
     * If you need custom logic inside this method you can override this class or choose not to use it.
     */
    open fun execute(request: Request): GraphQLServerResponse? {
        val graphQLRequest = requestParser.parseRequest(request) ?: return null
        val graphQLContext = contextFactory.generateContext(request)
        return requestHandler.executeRequest(graphQLRequest, graphQLContext)
    }
}
