package radiant.nimbus.api

import com.atproto.server.CreateSessionRequest
import com.atproto.server.CreateSessionResponse
import com.atproto.server.RefreshSessionResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.call.body
import io.ktor.client.call.save
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders.Authorization
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.contentType
import io.ktor.util.AttributeKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import radiant.nimbus.api.auth.Credentials
import radiant.nimbus.api.response.AtpErrorDescription

/**
 * Appends the `Authorization` header to XRPC requests, as well as automatically refreshing and
 * replaying a network request if it fails due to an expired access token.
 */
internal class XrpcAuthPlugin(
  private val json: Json,
  private val authTokens: MutableStateFlow<Tokens?>,
  private val authCredentials: MutableStateFlow<Credentials?> = MutableStateFlow(null),
) {
  class Config(
    var json: Json = Json { ignoreUnknownKeys = true },
    var authTokens: MutableStateFlow<Tokens?> = MutableStateFlow(null),
    var authCredentials: MutableStateFlow<Credentials?> = MutableStateFlow(null),
  )

  companion object : HttpClientPlugin<Config, XrpcAuthPlugin> {
    override val key = AttributeKey<XrpcAuthPlugin>("XrpcAuthPlugin")

    override fun prepare(block: Config.() -> Unit): XrpcAuthPlugin {
      val config = Config().apply(block)
      return XrpcAuthPlugin(config.json, config.authTokens, config.authCredentials)
    }

    override fun install(
      plugin: XrpcAuthPlugin,
      scope: HttpClient,
    ) {
      scope.plugin(HttpSend).intercept { context ->
        if (!context.headers.contains(Authorization)) {
          plugin.authTokens.value?.auth?.let { context.bearerAuth(it) }
        }

        var result: HttpClientCall = execute(context)
        if (result.response.status != BadRequest) {
          return@intercept result
        }

        // Cache the response in memory since we will need to decode it potentially more than once.
        result = result.save()

        val response = runCatching<AtpErrorDescription> {
          plugin.json.decodeFromString(result.response.bodyAsText())
        }

        if (response.getOrNull()?.error == "ExpiredToken") {
          if (response.getOrNull()?.message?.contains("Token has been revoked") == true && plugin.authCredentials.value != null) {
            val request = if(plugin.authCredentials.value?.username?.handle != null) {
              plugin.authCredentials.value?.password?.let {
                CreateSessionRequest(
                  plugin.authCredentials.value?.username?.handle!!,
                  it
                )
              }
            } else if(plugin.authCredentials.value?.email != null) {
              plugin.authCredentials.value?.password?.let {
                CreateSessionRequest(
                  plugin.authCredentials.value?.email!!,
                  it
                )
              }
            } else null

              val newSessionResponse = scope.post("/xrpc/com.atproto.server.createSession") {
                plugin.authTokens.value?.refresh?.let { bearerAuth(it) }
                contentType(ContentType.Application.Json)
                setBody(request)
              }
              runCatching { newSessionResponse.body<CreateSessionResponse>() }.getOrNull()?.let { new ->
                val newAccessToken = new.accessJwt
                val newRefreshToken = new.refreshJwt

                plugin.authTokens.value = Tokens(newAccessToken, newRefreshToken)
                context.headers.remove(Authorization)
                context.bearerAuth(newAccessToken)
                result = execute(context)
              }

          } else {
            val refreshResponse = scope.post("/xrpc/com.atproto.server.refreshSession") {
              plugin.authTokens.value?.refresh?.let { bearerAuth(it) }
            }
            runCatching { refreshResponse.body<RefreshSessionResponse>() }.getOrNull()?.let { refreshed ->
                val newAccessToken = refreshed.accessJwt
                val newRefreshToken = refreshed.refreshJwt

                plugin.authTokens.value = Tokens(newAccessToken, newRefreshToken)
                context.headers.remove(Authorization)
                context.bearerAuth(newAccessToken)
                result = execute(context)
            }
          }
        }

        result
      }
    }
  }
}
