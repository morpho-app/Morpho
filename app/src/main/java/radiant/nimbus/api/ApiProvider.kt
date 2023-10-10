package radiant.nimbus.api

import android.util.Log
import app.bsky.feed.Like
import app.bsky.feed.Repost
import com.atproto.repo.CreateRecordRequest
import com.atproto.repo.DeleteRecordRequest
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
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import radiant.nimbus.api.auth.AuthInfo
import radiant.nimbus.api.auth.Credentials
import radiant.nimbus.api.auth.LoginRepository
import radiant.nimbus.api.model.RecordType
import radiant.nimbus.api.model.RecordUnion
import radiant.nimbus.api.model.Timestamp
import radiant.nimbus.api.response.AtpResponse
import radiant.nimbus.app.Supervisor
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
  private val userCredentials = MutableStateFlow(loginRepository.credentials)




  private var client = HttpClient(CIO) {
    install(Logging) {
      logger = Logger.DEFAULT
      level = LogLevel.ALL
    }

    install(XrpcAuthPlugin) {
      authTokens = tokens
      authCredentials = userCredentials
    }

    install(DefaultRequest) {
      val hostUrl = Url(apiHost.value)
      url.protocol = hostUrl.protocol
      url.host = hostUrl.host
      url.port = hostUrl.port
    }

    expectSuccess = false
  }

  var api: BlueskyApi = XrpcBlueskyApi(client)

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
        loginRepository.credentials()
          .distinctUntilChanged()
          .collect {
            userCredentials.value = it
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

  fun credentials(): Flow<Credentials?> = userCredentials

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
          loginRepository.credentials = credentials
          Log.i("Login: ", loginRepository.auth.toString())
          Log.i("Login: ", userCredentials.value.toString())
          response
        }
      }
    }
  }

  suspend fun createRecord(
    record: RecordUnion
  ) : Deferred<String?> = CoroutineScope(Dispatchers.IO).async {
    val did = loginRepository.auth?.did
    val timestamp : Timestamp = Clock.System.now()
    if (did != null) {
      val request = when(record) {
        is RecordUnion.Like -> {
          val like = Like(record.subject, timestamp)
          CreateRecordRequest(
            repo = AtIdentifier(did.did),
            collection = record.type.collection,
            record = Json.encodeToJsonElement(value = like)
          )
        }
        is RecordUnion.MakePost -> {
          CreateRecordRequest(
            repo = AtIdentifier(did.did),
            collection = record.type.collection,
            record = Json.encodeToJsonElement(value = record.post)
          )
        }
        is RecordUnion.Repost -> {
          val repost = Repost(record.subject, timestamp)
          CreateRecordRequest(
            repo = AtIdentifier(did.did),
            collection = record.type.collection,
            record = Json.encodeToJsonElement(value = repost)
          )
        }
      }
      api.createRecord(request).maybeResponse()?.cid?.cid
    } else {
      null
    }
  }

  fun deleteRecord(type: RecordType, rkey: String) = CoroutineScope(Dispatchers.IO).launch {
    val did = loginRepository.auth?.did
    if (did != null) {
      api.deleteRecord(DeleteRecordRequest(AtIdentifier(did.did), type.collection, rkey))
    }
  }
}
