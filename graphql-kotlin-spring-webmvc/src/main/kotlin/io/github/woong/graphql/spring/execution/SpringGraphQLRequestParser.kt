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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.MapType
import com.fasterxml.jackson.databind.type.TypeFactory
import io.github.woong.graphql.server.execution.GraphQLRequestParser
import io.github.woong.graphql.server.types.GraphQLRequest
import io.github.woong.graphql.server.types.GraphQLServerRequest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.body
import kotlin.jvm.optionals.getOrNull

internal const val REQUEST_PARAM_QUERY = "query"
internal const val REQUEST_PARAM_OPERATION_NAME = "operationName"
internal const val REQUEST_PARAM_VARIABLES = "variables"
internal const val REQUEST_PARAM_EXTENSIONS = "extensions"
internal const val REQUEST_PARAM_PERSISTED_QUERY = "persistedQuery"
internal val graphQLMediaType = MediaType("application", "graphql")

open class SpringGraphQLRequestParser(
    private val objectMapper: ObjectMapper,
) : GraphQLRequestParser<ServerRequest> {
    private val mapTypeReference: MapType =
        TypeFactory.defaultInstance().constructMapType(
            HashMap::class.java,
            String::class.java,
            Any::class.java,
        )

    override fun parseRequest(request: ServerRequest): GraphQLServerRequest? =
        when {
            request.isGetPersistedQuery() || request.hasQueryParam() -> {
                getRequestFromGet(request)
            }

            request.method() == HttpMethod.POST -> getRequestFromPost(request)
            else -> null
        }

    private fun ServerRequest.hasQueryParam() = param(REQUEST_PARAM_QUERY).isPresent

    private fun ServerRequest.isGetPersistedQuery() =
        method() == HttpMethod.GET &&
            param(REQUEST_PARAM_EXTENSIONS)
                .getOrNull()
                ?.contains(REQUEST_PARAM_PERSISTED_QUERY) == true

    private fun getRequestFromGet(serverRequest: ServerRequest): GraphQLServerRequest {
        val query = serverRequest.param(REQUEST_PARAM_QUERY).orElse("")
        val operationName: String? = serverRequest.param(REQUEST_PARAM_OPERATION_NAME).orElseGet { null }
        val variables: String? = serverRequest.param(REQUEST_PARAM_VARIABLES).orElseGet { null }
        val graphQLVariables: Map<String, Any>? =
            variables?.let {
                objectMapper.readValue(it, mapTypeReference)
            }
        val extensions: Map<String, Any>? =
            serverRequest.param(REQUEST_PARAM_EXTENSIONS).takeIf { it.isPresent }?.get()?.let {
                objectMapper.readValue(it, mapTypeReference)
            }

        return GraphQLRequest(
            query = query,
            operationName = operationName,
            variables = graphQLVariables,
            extensions = extensions,
        )
    }

    private fun getRequestFromPost(serverRequest: ServerRequest): GraphQLServerRequest? {
        val contentType = serverRequest.headers().contentType().orElse(MediaType.APPLICATION_JSON)
        return when {
            contentType.includes(MediaType.APPLICATION_JSON) -> serverRequest.body(GraphQLServerRequest::class.java)
            contentType.includes(graphQLMediaType) -> GraphQLRequest(query = serverRequest.body())
            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Content-Type is not specified")
        }
    }
}
