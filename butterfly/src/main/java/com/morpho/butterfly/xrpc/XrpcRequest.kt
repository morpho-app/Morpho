package com.morpho.butterfly.xrpc

import com.morpho.butterfly.response.AtpError
import com.morpho.butterfly.response.AtpException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.wss
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.websocket.Frame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.ExperimentalSerializationApi
import morpho.app.api.response.StatusCode

suspend inline fun HttpClient.query(
  path: String,
  queryParams: List<Pair<String, Any?>> = emptyList(),
): HttpResponse {
  return get(path) {
    queryParams.forEach { (key, value) -> parameter(key, value) }
  }
}

suspend inline fun HttpClient.procedure(path: String): HttpResponse {
  return post(path)
}

suspend inline fun <reified T : Any> HttpClient.procedure(
  path: String,
  body: T,
  encoding: String,
): HttpResponse {
  return post(path) {
    headers["Content-Type"] = encoding
    setBody(body)
  }
}

suspend fun HttpClient.subscription(
  path: String,
  queryParams: List<Pair<String, Any?>> = emptyList(),
): Flow<XrpcSubscriptionResponse> = flow {
  wss(
    path = path,
    request = { queryParams.forEach { (key, value) -> parameter(key, value) } },
  ) {
    emitAll(
      incoming.receiveAsFlow()
        .filterIsInstance<Frame.Binary>()
        .map { frame -> XrpcSubscriptionResponse(frame.data) }
    )
  }
}

suspend inline fun <reified T : Any> HttpResponse.toAtpModel(): T {
  val headers = headers.entries().associateByTo(mutableMapOf(), { it.key }, { it.value.last() })
  return when (val status = StatusCode.fromCode(status.value)) {
    is StatusCode.Okay -> body<T>()
    is StatusCode.Failure -> throw AtpException(status, headers, AtpError(status::class.simpleName, bodyAsText()))
  }
}

inline fun <reified T : Any> Flow<XrpcSubscriptionResponse>.toAtpModel(): Flow<T> =
  toAtpResult<T>().map { it.getOrThrow() }

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T : Any> Flow<XrpcSubscriptionResponse>.toAtpResult(): Flow<Result<T>> =
  map { response -> runCatching { response.body<T>() } }

suspend inline fun <reified T : Any> HttpResponse.toAtpResult(): Result<T> {
  val headers = headers.entries().associateByTo(mutableMapOf(), { it.key }, { it.value.last() })

  return when (val code = StatusCode.fromCode(status.value)) {
    is StatusCode.Okay -> {
      Result.success(body())
    }
    is StatusCode.Failure -> {
      val maybeBody = runCatching<T> { body() }.getOrNull()
      val maybeError = if (maybeBody == null) {
        kotlin.runCatching<AtpError> { body() }.getOrNull()
      } else {
        null
      }

      return Result.failure(
        AtpException(code, headers, maybeError)
      )
    }
  }
}

inline fun <reified T : Any> Flow<XrpcSubscriptionResponse>.toAtpResponse(): Flow<Result<T>> =
  toAtpResult<T>().map {
    it.fold(
      onSuccess = { body ->
        Result.success(body)
      },
      onFailure = { e ->
        Result.failure(
          AtpException(
            statusCode = StatusCode.InvalidRequest,
            headers = emptyMap(),
            error = (e as? XrpcSubscriptionParseException)?.error,
          )
        )
      }
    )
  }