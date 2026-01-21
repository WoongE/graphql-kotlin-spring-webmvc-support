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

import com.expediagroup.graphql.dataloader.KotlinDataLoaderRegistry
import com.expediagroup.graphql.dataloader.KotlinDataLoaderRegistryFactory
import com.expediagroup.graphql.dataloader.instrumentation.syncexhaustion.DataLoaderSyncExecutionExhaustedInstrumentation
import com.expediagroup.graphql.dataloader.instrumentation.syncexhaustion.state.SyncExecutionExhaustedState
import com.expediagroup.graphql.generator.extensions.plus
import graphql.GraphQL
import graphql.GraphQLContext
import graphql.execution.instrumentation.ChainedInstrumentation
import graphql.execution.instrumentation.Instrumentation
import io.github.woong.graphql.server.extensions.containsMutation
import io.github.woong.graphql.server.extensions.isBatchDataLoaderInstrumentation
import io.github.woong.graphql.server.extensions.toExecutionInput
import io.github.woong.graphql.server.extensions.toGraphQLResponse
import io.github.woong.graphql.server.types.GraphQLBatchRequest
import io.github.woong.graphql.server.types.GraphQLBatchResponse
import io.github.woong.graphql.server.types.GraphQLRequest
import io.github.woong.graphql.server.types.GraphQLResponse
import io.github.woong.graphql.server.types.GraphQLServerRequest
import io.github.woong.graphql.server.types.GraphQLServerResponse
import java.util.concurrent.CompletableFuture

open class GraphQLRequestHandler(
    private val graphQL: GraphQL,
    private val dataLoaderRegistryFactory: KotlinDataLoaderRegistryFactory? = null,
) {
    private val batchDataLoaderInstrumentationType: Class<Instrumentation>? =
        graphQL.instrumentation?.let { instrumentation ->
            when {
                instrumentation is ChainedInstrumentation -> {
                    instrumentation.instrumentations
                        .firstOrNull(Instrumentation::isBatchDataLoaderInstrumentation)
                        ?.javaClass
                }

                instrumentation.isBatchDataLoaderInstrumentation() -> instrumentation.javaClass
                else -> null
            }
        }

    /**
     * Execute a GraphQL request in a blocking fashion.
     * This should only be used for queries and mutations.
     * Subscriptions require more specific server logic and will need to be handled separately.
     */
    open fun executeRequest(
        graphQLRequest: GraphQLServerRequest,
        graphQLContext: GraphQLContext = GraphQLContext.of(emptyMap<Any, Any>()),
    ): GraphQLServerResponse {
        val dataLoaderRegistry = dataLoaderRegistryFactory?.generate(graphQLContext)
        return when (graphQLRequest) {
            is GraphQLRequest -> {
                val batchGraphQLContext = graphQLContext + getBatchContext(1, dataLoaderRegistry)
                execute(graphQLRequest, batchGraphQLContext, dataLoaderRegistry)
            }

            is GraphQLBatchRequest -> {
                if (graphQLRequest.containsMutation()) {
                    val batchGraphQLContext = graphQLContext + getBatchContext(1, dataLoaderRegistry)
                    executeSequentially(graphQLRequest, batchGraphQLContext, dataLoaderRegistry)
                } else {
                    val batchGraphQLContext =
                        graphQLContext + getBatchContext(graphQLRequest.requests.size, dataLoaderRegistry)
                    executeConcurrently(graphQLRequest, batchGraphQLContext, dataLoaderRegistry)
                }
            }
        }
    }

    private fun execute(
        graphQLRequest: GraphQLRequest,
        batchGraphQLContext: GraphQLContext,
        dataLoaderRegistry: KotlinDataLoaderRegistry?,
    ): GraphQLResponse<*> =
        runCatching {
            graphQL
                .executeAsync(graphQLRequest.toExecutionInput(batchGraphQLContext, dataLoaderRegistry))
                .join()
                .toGraphQLResponse()
        }.getOrElse { GraphQLResponse.error(it) }

    private fun executeSequentially(
        batchRequest: GraphQLBatchRequest,
        batchGraphQLContext: GraphQLContext,
        dataLoaderRegistry: KotlinDataLoaderRegistry?,
    ): GraphQLBatchResponse {
        val responses =
            batchRequest.requests
                .map { execute(it, batchGraphQLContext, dataLoaderRegistry) }
        return GraphQLBatchResponse(responses)
    }

    @Suppress("SpreadOperator")
    private fun executeConcurrently(
        batchRequest: GraphQLBatchRequest,
        batchGraphQLContext: GraphQLContext,
        dataLoaderRegistry: KotlinDataLoaderRegistry?,
    ): GraphQLBatchResponse {
        val futures =
            batchRequest.requests
                .map { graphQL.executeAsync(it.toExecutionInput(batchGraphQLContext, dataLoaderRegistry)) }
        val responses =
            CompletableFuture
                .allOf(*futures.toTypedArray())
                .thenApply {
                    futures.map { future ->
                        runCatching { future.join().toGraphQLResponse() }
                            .getOrElse { GraphQLResponse.error(it) }
                    }
                }.join()
        return GraphQLBatchResponse(responses)
    }

    private fun getBatchContext(
        batchSize: Int,
        dataLoaderRegistry: KotlinDataLoaderRegistry?,
    ): Map<*, Any> {
        dataLoaderRegistry ?: return emptyMap<Any, Any>()
        return when (batchDataLoaderInstrumentationType) {
            DataLoaderSyncExecutionExhaustedInstrumentation::class.java ->
                mapOf(
                    SyncExecutionExhaustedState::class to SyncExecutionExhaustedState(batchSize, dataLoaderRegistry),
                )

            else -> emptyMap<Any, Any>()
        }
    }
}
