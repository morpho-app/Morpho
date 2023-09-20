package radiant.nimbus.api

import com.atproto.server.CreateSessionRequest
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import radiant.nimbus.api.auth.AuthInfo
import radiant.nimbus.api.auth.Credentials
import radiant.nimbus.api.auth.LoginRepository
import radiant.nimbus.app.Supervisor
import sh.christian.ozone.BlueskyApi
import sh.christian.ozone.XrpcBlueskyApi
import sh.christian.ozone.api.response.AtpResponse
import javax.inject.Inject
import javax.inject.Singleton

//@SingleInApp
@Singleton
class ApiProvider @Inject constructor(
  private val apiRepository: ServerRepository,
  val loginRepository: LoginRepository,
) : Supervisor {

  private val apiHost = MutableStateFlow(apiRepository.server!!.host)
  private val auth = MutableStateFlow(loginRepository.auth)
  private val tokens = MutableStateFlow(loginRepository.auth?.toTokens())

  private val client = HttpClient(CIO) {
    install(Logging) {
      logger = Logger.DEFAULT
      level = LogLevel.ALL
    }

    install(XrpcAuthPlugin) {
      authTokens = tokens
    }

    install(DefaultRequest) {
      val hostUrl = Url(apiHost.value)
      url.protocol = hostUrl.protocol
      url.host = hostUrl.host
      url.port = hostUrl.port
    }

    expectSuccess = false
  }

  val api: BlueskyApi = XrpcBlueskyApi(client)

  override suspend fun CoroutineScope.onStart() {
    coroutineScope {
      launch(Dispatchers.IO) {
        apiRepository.server().map { it.host }
          .distinctUntilChanged()
          .collect(apiHost)
      }

      launch(Dispatchers.IO) {
        loginRepository.auth()
          .distinctUntilChanged()
          .collect {
            tokens.value = it?.toTokens()
            yield()
            auth.value = it
          }
      }

      launch(Dispatchers.IO) {
        tokens.collect { tokens ->
          if (tokens != null) {
            loginRepository.auth = loginRepository.auth().first()!!.withTokens(tokens)
          } else {
            loginRepository.auth = null
          }
        }
      }
    }
  }


  fun auth(): Flow<AuthInfo?> = auth

  private fun AuthInfo.toTokens() = Tokens(accessJwt, refreshJwt)

  private fun AuthInfo.withTokens(tokens: Tokens) = copy(
    accessJwt = tokens.auth,
    refreshJwt = tokens.refresh,
  )

  suspend fun makeLoginRequest(credentials: Credentials): AtpResponse<AuthInfo> {
    return withContext(Dispatchers.IO) {
      val request = CreateSessionRequest(credentials.username.handle, credentials.password)
      val response = api.createSession(request).map { response ->
        AuthInfo(
          accessJwt = response.accessJwt,
          refreshJwt = response.refreshJwt,
          handle = response.handle,
          did = response.did,
        )
      }
      when(response) {
        is AtpResponse.Failure -> response
        is AtpResponse.Success -> {
          loginRepository.auth = response.response
          response
        }
      }
    }
  }
}
