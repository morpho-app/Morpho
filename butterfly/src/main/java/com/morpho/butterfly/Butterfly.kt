package com.morpho.butterfly

import app.bsky.actor.GetPreferencesResponse
import app.bsky.feed.Like
import app.bsky.feed.Repost
import com.atproto.repo.CreateRecordRequest
import com.atproto.repo.DeleteRecordRequest
import com.atproto.server.CreateSessionRequest
import com.atproto.server.RefreshSessionResponse
import com.morpho.butterfly.auth.AuthInfo
import com.morpho.butterfly.auth.Credentials
import com.morpho.butterfly.auth.LoginRepository
import com.morpho.butterfly.auth.RelayRepository
import com.morpho.butterfly.model.RecordType
import com.morpho.butterfly.model.RecordUnion
import com.morpho.butterfly.model.Timestamp
import com.morpho.butterfly.storage.RkeyCacheEntry
import com.morpho.butterfly.xrpc.XrpcBlueskyApi
import com.morpho.butterfly.xrpc.toAtpResult
import com.morpho.butterfly.xrpc.withXrpcConfiguration
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.cache.storage.*
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.json.encodeToJsonElement
import okio.FileSystem
import okio.Path.Companion.toPath

private const val TAG = "butterfly"

class Butterfly(
    private val relay: RelayRepository,
    private val user: LoginRepository
) {

    // TODO: implement this cache in a better way
    private val rkeyCache: MutableMap<AtUri, RkeyCacheEntry> = mutableMapOf()

    var atpClient = HttpClient(CIO) {

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
        }

        install(HttpCache) {
            val cache = "./cache".toPath().relativeTo(FileSystem.SYSTEM_TEMPORARY_DIRECTORY).toFile()
            publicStorage(FileStorage(cache))
        }

        defaultRequest {
            val hostUrl = Url(relay.server.host)
            url.protocol = hostUrl.protocol
            url.host = hostUrl.host
            url.port = hostUrl.port
        }

        install(Auth) {
            bearer {
                loadTokens {
                    user.auth?.toTokens()
                }

                refreshTokens {
                    val refresh = user.auth?.refreshJwt
                    val refreshResponse:AuthInfo = client.post("/xrpc/com.atproto.server.refreshSession") {
                        if (refresh != null) {
                            bearerAuth(refresh)
                        }
                        markAsRefreshTokenRequest()
                    }.body()
                    user.auth = refreshResponse
                    refreshResponse.toTokens()
                }
                realm

                //sendWithoutRequest {
                // figure out how to detect xrpc api calls that don't need authentication
                //}
            }
        }
    }

    var api: BlueskyApi = XrpcBlueskyApi(atpClient)

    private fun AuthInfo.toTokens() = BearerTokens(accessJwt, refreshJwt)

    private fun AuthInfo.withTokens(tokens: BearerTokens) = copy(
        accessJwt = tokens.accessToken,
        refreshJwt = tokens.refreshToken,
    )

    // Pulled this out of where I stuck it in the API so it doesn't get overwritten
    // TODO: Figure out root cause of why that first normal refresh fucks up, wtf did Christian do?
    suspend fun refreshSession(auth: AuthInfo): Result<RefreshSessionResponse> {
        return atpClient.withXrpcConfiguration().post("/xrpc/com.atproto.server.refreshSession") {
            this.bearerAuth(auth.refreshJwt)
        }.toAtpResult()
    }

    suspend fun getUserPreferences() : Result<GetPreferencesResponse> {
        return api.getPreferences()
    }


    suspend fun makeLoginRequest(credentials: Credentials): Result<AuthInfo> = runCatching {
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
            response.onSuccess {
                user.auth = response.getOrThrow()
                user.credentials = credentials

            }
        }
    }

    fun createRecord(
        record: RecordUnion
    ) = CoroutineScope(Dispatchers.IO).launch {
        val did = user.auth?.did
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
            val rkey = getRkey(api.createRecord(request).getOrNull()?.uri)
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
                deleteRecord(type, rkey)
            }
        }
    }

    private fun deleteRecord(type: RecordType, rkey: String) = CoroutineScope(Dispatchers.IO).launch {
        val did = user.auth?.did
        if (did != null) {
            api.deleteRecord(DeleteRecordRequest(AtIdentifier(did.did), type.collection, rkey))
        }
    }

}

