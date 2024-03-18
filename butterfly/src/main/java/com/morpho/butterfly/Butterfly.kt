package com.morpho.butterfly

import com.morpho.butterfly.auth.AuthInfo
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

class Butterfly(
    appViewHost: String,
    authInfo: AuthInfo,
) {
    val jwtStorage = mutableListOf<BearerTokens>()

    val atprotoClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }

        defaultRequest {
            val hostUrl = Url(appViewHost)
            url.protocol = hostUrl.protocol
            url.host = hostUrl.host
            url.port = hostUrl.port
        }

        install(Auth) {
            bearer {
                loadTokens {
                    jwtStorage.last()
                }

                refreshTokens {
                    val refreshResponse:AuthInfo = client.post("/xrpc/com.atproto.server.refreshSession") {
                        bearerAuth(jwtStorage.last().refreshToken)
                        markAsRefreshTokenRequest()
                    }.body()
                    jwtStorage.add(refreshResponse.toTokens())
                    jwtStorage.last()
                }

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

