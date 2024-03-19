package com.morpho.butterfly

import com.morpho.butterfly.auth.AuthInfo
import com.morpho.butterfly.auth.LoginRepository
import com.morpho.butterfly.auth.RelayRepository
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
import okio.FileSystem
import okio.Path.Companion.toPath


class Butterfly(
    private val relay: RelayRepository,
    private val user: LoginRepository
) {


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

    private fun AuthInfo.toTokens() = BearerTokens(accessJwt, refreshJwt)

    private fun AuthInfo.withTokens(tokens: BearerTokens) = copy(
        accessJwt = tokens.accessToken,
        refreshJwt = tokens.refreshToken,
    )

}

