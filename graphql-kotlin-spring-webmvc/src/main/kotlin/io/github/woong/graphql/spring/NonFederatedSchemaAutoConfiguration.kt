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

package io.github.woong.graphql.spring

import com.expediagroup.graphql.generator.ClasspathTypeResolver
import com.expediagroup.graphql.generator.GraphQLTypeResolver
import com.expediagroup.graphql.generator.SchemaGeneratorConfig
import com.expediagroup.graphql.generator.TopLevelNames
import com.expediagroup.graphql.generator.execution.KotlinDataFetcherFactoryProvider
import com.expediagroup.graphql.generator.extensions.print
import com.expediagroup.graphql.generator.hooks.NoopSchemaGeneratorHooks
import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import com.expediagroup.graphql.generator.internal.state.ClassScanner
import com.expediagroup.graphql.generator.toSchema
import graphql.schema.GraphQLSchema
import io.github.woong.graphql.server.operations.Mutation
import io.github.woong.graphql.server.operations.Query
import io.github.woong.graphql.server.operations.Subscription
import io.github.woong.graphql.spring.extensions.toTopLevelObjects
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import java.util.Optional

/**
 * SpringBoot autoconfiguration for generating a non-federated GraphQL schema object.
 */
@ConditionalOnProperty(value = ["graphql.federation.enabled"], havingValue = "false", matchIfMissing = true)
@Configuration
@Import(GraphQLExecutionConfiguration::class)
class NonFederatedSchemaAutoConfiguration(
    private val config: GraphQLConfigurationProperties,
) {
    private val logger = LoggerFactory.getLogger(NonFederatedSchemaAutoConfiguration::class.java)

    @Bean
    @ConditionalOnMissingBean
    fun typeResolver(): GraphQLTypeResolver = ClasspathTypeResolver(ClassScanner(config.packages))

    @Bean
    @ConditionalOnMissingBean
    fun schemaConfig(
        topLevelNames: Optional<TopLevelNames>,
        hooks: Optional<SchemaGeneratorHooks>,
        dataFetcherFactoryProvider: KotlinDataFetcherFactoryProvider,
        typeResolver: GraphQLTypeResolver,
    ): SchemaGeneratorConfig =
        SchemaGeneratorConfig(
            supportedPackages = config.packages,
            topLevelNames = topLevelNames.orElse(TopLevelNames()),
            hooks = hooks.orElse(NoopSchemaGeneratorHooks),
            dataFetcherFactoryProvider = dataFetcherFactoryProvider,
            introspectionEnabled = config.introspection.enabled,
            typeResolver = typeResolver,
        )

    @Bean
    @ConditionalOnMissingBean
    fun schema(
        queries: Optional<List<Query>>,
        mutations: Optional<List<Mutation>>,
        subscriptions: Optional<List<Subscription>>,
        schemaConfig: SchemaGeneratorConfig,
    ): GraphQLSchema =
        toSchema(
            config = schemaConfig,
            queries = queries.orElse(emptyList()).toTopLevelObjects(),
            mutations = mutations.orElse(emptyList()).toTopLevelObjects(),
            subscriptions = subscriptions.orElse(emptyList()).toTopLevelObjects(),
        ).also { schema ->
            if (config.printSchema) {
                logger.info("\n${schema.print()}")
            }
        }
}
