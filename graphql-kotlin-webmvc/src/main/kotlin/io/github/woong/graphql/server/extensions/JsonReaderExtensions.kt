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

package io.github.woong.graphql.server.extensions

import com.alibaba.fastjson2.JSONReader

/**
 * Reads a JSON array and deserializes each element to the specified type.
 */
inline fun <reified T> JSONReader.readAsArray(): List<T> {
    val collector = mutableListOf<T>()
    readArray(collector, T::class.java)
    return collector
}

/**
 * Reads a JSON object and deserializes it to the specified type.
 */
inline fun <reified T> JSONReader.readAs(): T = read(T::class.java)
