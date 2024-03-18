package com.morpho.butterfly.response

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import morpho.app.api.response.StatusCode
import java.io.IOException

public class AtpException(
  val statusCode: StatusCode,
  val headers: Map<String, String>,
  val error: AtpError?
) : IOException("XRPC request failed: ${statusCode::class.simpleName}, Error: ${{ error?.error }} | ${{ error?.message }}")

@Serializable
public data class AtpError(
  val error: String?,
  val message: String?,
)