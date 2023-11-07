// Copyright 2023 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.firebase.dataconnect

import com.google.firebase.concurrent.FirebaseExecutors
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*

class QuerySubscription<VariablesType, ResultType>
internal constructor(
  internal val query: QueryRef<VariablesType, ResultType>,
  variables: VariablesType
) {
  private val _variables = AtomicReference(variables)
  val variables: VariablesType
    get() = _variables.get()

  private val sharedFlow =
    MutableSharedFlow<Message<VariablesType, ResultType>>(
      replay = 1,
      extraBufferCapacity = Integer.MAX_VALUE
    )
  private val sequentialDispatcher =
    FirebaseExecutors.newSequentialExecutor(query.dataConnect.nonBlockingExecutor)
      .asCoroutineDispatcher()

  // NOTE: The variables below must ONLY be accessed from coroutines that use `sequentialDispatcher`
  // for their `CoroutineDispatcher`. Having this requirement removes the need for explicitly
  // synchronizing access to these variables.
  private var reloadInProgress = false
  private var pendingReload = false

  val lastResult: Message<VariablesType, ResultType>?
    get() = sharedFlow.replayCache.firstOrNull()

  fun reload() {
    query.dataConnect.coroutineScope.launch(sequentialDispatcher) {
      pendingReload = true
      if (!reloadInProgress) {
        reloadInProgress = true
        try {
          doReloadLoop()
        } finally {
          reloadInProgress = false
        }
      }
    }
  }

  fun update(variables: VariablesType) {
    _variables.set(variables)
    reload()
  }

  val flow: Flow<Message<VariablesType, ResultType>>
    get() = sharedFlow.asSharedFlow().onSubscription { reload() }.buffer(Channel.CONFLATED)

  private suspend fun doReloadLoop() {
    while (pendingReload) {
      pendingReload = false
      sharedFlow.emit(reload(variables, query))
    }
  }

  class Message<VariablesType, ResultType>(
    val variables: VariablesType,
    val result: Result<ResultType>
  )
}

private suspend fun <VariablesType, ResultType> reload(
  variables: VariablesType,
  query: QueryRef<VariablesType, ResultType>
) =
  QuerySubscription.Message(
    variables = variables,
    result =
      try {
        Result.success(query.execute(variables))
      } catch (e: Throwable) {
        Result.failure(e)
      }
  )