package morpho.app.api

import android.util.Log
import app.bsky.actor.GetPreferencesResponse
import app.bsky.feed.Like
import app.bsky.feed.Repost
import com.atproto.repo.CreateRecordRequest
import com.atproto.repo.DeleteRecordRequest
import com.atproto.server.CreateSessionRequest
import com.atproto.server.RefreshSessionResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
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
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.encodeToJsonElement
import morpho.app.api.auth.AuthInfo
import morpho.app.api.auth.Credentials
import morpho.app.api.auth.LoginRepository
import morpho.app.api.model.ReadOnlyList
import morpho.app.api.model.RecordType
import morpho.app.api.model.RecordUnion
import morpho.app.api.model.Timestamp
import morpho.app.api.response.AtpResponse
import morpho.app.api.xrpc.toAtpResponse
import morpho.app.api.xrpc.withXrpcConfiguration
import morpho.app.app.Supervisor
import morpho.app.util.getRkey
import morpho.app.util.json
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.set

@Serializable
data class RkeyCacheEntry(
  var likeKey: String = "",
  var repostKey: String = "",
  var postKey: String = "",
)

private const val TAG = "API_Provider"

@Singleton
class ApiProvider @Inject constructor(
  private val apiRepository: ServerRepository,
  val loginRepository: LoginRepository,
) : Supervisor {

  private val apiHost = MutableStateFlow(apiRepository.server!!.host)
  private val auth = MutableStateFlow(loginRepository.auth)
  private val tokens = MutableStateFlow(loginRepository.auth?.toTokens())
  private val userCredentials = MutableStateFlow(loginRepository.credentials)

  // TODO: implement this cache in a better way
  private val rkeyCache: MutableMap<AtUri, RkeyCacheEntry> = mutableMapOf()


  private var client = HttpClient(CIO) {
    install(Logging) {
      logger = Logger.DEFAULT
      level = LogLevel.HEADERS
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
    install(HttpRequestRetry) {
      retryOnServerErrors(maxRetries = 5)
      exponentialDelay()
    }
    install(HttpTimeout) {
      requestTimeoutMillis = Long.MAX_VALUE
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


  // Pulled this out of where I stuck it in the API so it doesn't get overwritten
  // TODO: Figure out root cause of why that first normal refresh fucks up, wtf did Christian do?
  suspend fun refreshSession(auth: AuthInfo): AtpResponse<RefreshSessionResponse> {
    return client.withXrpcConfiguration().post("/xrpc/com.atproto.server.refreshSession") {
      this.bearerAuth(auth.refreshJwt)
    }.toAtpResponse()
  }

  suspend fun getUserPreferences() : AtpResponse<GetPreferencesResponse> {
    return api.getPreferences()
  }


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
          auth.value = response.response
          loginRepository.credentials = credentials
          userCredentials.value = credentials
          Log.i(TAG, "Login: ${loginRepository.auth}")
          Log.i(TAG,"Login: ${userCredentials.value}")

          response
        }
      }
    }
  }

  fun createRecord(
    record: RecordUnion
  ) = CoroutineScope(Dispatchers.IO).launch {
    val did = loginRepository.auth?.did
    val timestamp : Timestamp = Clock.System.now()
    val uri: AtUri
    if (did != null) {
      val request = when(record) {
        is RecordUnion.Like -> {
          uri = record.subject.uri
          val like = Like(record.subject, timestamp)
          CreateRecordRequest(
            repo = AtIdentifier(did.did),
            collection = record.type.collection,
            record = json.encodeToJsonElement(value = like)
          )
        }
        is RecordUnion.MakePost -> {
          uri = AtUri("$did/${record.type.collection}/$timestamp")
          CreateRecordRequest(
            repo = AtIdentifier(did.did),
            collection = record.type.collection,
            record = json.encodeToJsonElement(value = record.post)
          )
        }
        is RecordUnion.Repost -> {
          uri = record.subject.uri
          val repost = Repost(record.subject, timestamp)
          CreateRecordRequest(
            repo = AtIdentifier(did.did),
            collection = record.type.collection,
            record = json.encodeToJsonElement(value = repost)
          )
        }
      }
      val rkey = getRkey(api.createRecord(request).maybeResponse()?.uri)
      Log.i( TAG,"Rkey for creating ${record.type}: $rkey")
      when(record) {
        is RecordUnion.Like -> {
          if (rkeyCache.containsKey(uri)) {
            rkeyCache[uri]?.likeKey = rkey
          } else {
            rkeyCache[uri] = RkeyCacheEntry(likeKey = rkey)
          }
        }
        is RecordUnion.MakePost -> {
          if (rkeyCache.containsKey(uri)) {
            rkeyCache[uri]?.postKey = rkey
          } else {
            rkeyCache[uri] = RkeyCacheEntry(postKey = rkey)
          }
        }
        is RecordUnion.Repost -> if (rkeyCache.containsKey(uri)) {
          rkeyCache[uri]?.repostKey = rkey
        } else {
          rkeyCache[uri] = RkeyCacheEntry(repostKey = rkey)
        }
      }
    }
  }
  fun deleteRecord(type: RecordType, uri: AtUri?) {
    if (uri != null) {
      // If this is the right kind of uri for the record, we can use the last bit as the rkey
      val rkey = if(uri.atUri.contains(type.collection.nsid)) {
          getRkey(uri)
      } else {
        // Otherwise, we check our cache for it
        when(type) {
          RecordType.Post -> rkeyCache[uri]?.postKey
          RecordType.Like -> rkeyCache[uri]?.likeKey
          RecordType.Repost -> rkeyCache[uri]?.repostKey
        }
      }
      if (rkey != null) {
        Log.i( TAG,"Rkey for deleting $type: $rkey")
        deleteRecord(type, rkey)
      }
    }
  }

  private fun deleteRecord(type: RecordType, rkey: String) = CoroutineScope(Dispatchers.IO).launch {
    val did = loginRepository.auth?.did
    if (did != null) {
      api.deleteRecord(DeleteRecordRequest(AtIdentifier(did.did), type.collection, rkey))
    }
  }
}

@Serializable
public data class SkyFeedBuilderFeedsPref(
  /**
   * List of feeds
   */
  public val feeds: ReadOnlyList<AtUri>,
)