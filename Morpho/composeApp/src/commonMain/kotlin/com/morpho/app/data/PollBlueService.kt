package com.morpho.app.data

import com.morpho.butterfly.auth.UserRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLProtocol
import io.ktor.http.isSuccess
import io.ktor.http.path
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lighthousegames.logging.logging

private const val TAG = "poll.blue"

const val POLL_BLUE_DOMAIN = "poll.blue"
const val POLL_BLUE_API_PATH = "api/poll"
const val POLL_BLUE_VOTE_PATH = "p"
const val POLL_BLUE_VOTE_PARAM = "v"

public typealias PollBlueId = String

@Serializable
public sealed interface PollBlueVote {
    @Serializable
    public data class Voted(val chosen: Int): PollBlueVote
    @Serializable
    public data object NotVoted: PollBlueVote
}

const val POLL_OPTION_CHARACTERS =  "[1️⃣2️⃣3️⃣4️⃣🅰🅱🅲🅳]"

fun stripPollOptionCharacters(string: String): String {
    return string.replace(POLL_OPTION_CHARACTERS.toRegex(), "")
}


class PollBlueService: KoinComponent {

    companion object {
        val log = logging()
        val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    val userService: UserRepository by inject()

    val pollBlueCache = mutableMapOf<PollBlueId, PollBlueVote>()
    val pollBlueResultsCache = mutableMapOf<PollBlueId, List<Pair<String, Int>>>()

    val pollBlueClient = HttpClient(CIO) {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }

        install(HttpCache) {
            //publicStorage(getPlatformCache())
        }

        defaultRequest {
            url{
                host = POLL_BLUE_DOMAIN
                protocol = URLProtocol.HTTPS
            }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = Long.MAX_VALUE
        }

        expectSuccess = true
    }

    public fun lookupPollBlueVote(pollBlueId: PollBlueId): PollBlueVote {
        return pollBlueCache[pollBlueId] ?: PollBlueVote.NotVoted
    }
    public fun getCachedResults(pollBlueId: PollBlueId): List<Pair<String, Int>>? {
        return pollBlueResultsCache[pollBlueId]
    }

    public suspend fun getPollBlueResults(pollBlueId: PollBlueId): List<Pair<String, Int>> {
        runCatching {
            pollBlueClient.get {
                url {
                    path(POLL_BLUE_API_PATH, pollBlueId)
                }
            }
        }.onSuccess { response ->
            log.i { "Got poll blue results for $pollBlueId: $response" }
            log.i { response.bodyAsText() }
            val jsonResponse = Json.parseToJsonElement(response.bodyAsText())
            val results = jsonResponse.jsonObject["results"]?.jsonArray ?: return emptyList()
            val questions = jsonResponse.jsonObject["answers"]?.jsonArray ?: return emptyList()
            val pollResults = questions.map { it.jsonPrimitive.content }
                .zip(results.map { it.jsonPrimitive.content.toInt() }.takeLast(questions.size))
            log.i { "Results\n $pollResults" }
            pollBlueResultsCache[pollBlueId] = pollResults
            return pollResults
        }.onFailure {
            log.e { "Failed to get poll blue results for $pollBlueId: $it" }
        }
        return emptyList()
    }
    public suspend fun vote(pollBlueId: PollBlueId, option: Int) : PollBlueVote {
        require(option in 1..4) { "Option must be between 1 and 4" }
        runCatching {
            pollBlueClient.get {
                url {
                    host = POLL_BLUE_DOMAIN
                    path(POLL_BLUE_VOTE_PATH, pollBlueId)
                    parameter(POLL_BLUE_VOTE_PARAM, option)

                }
            }
        }.onSuccess {
            log.i { "Voted for $pollBlueId: $it" }
            if(it.status.isSuccess()) {
                pollBlueCache[pollBlueId] = PollBlueVote.Voted(option)
            }
            return PollBlueVote.Voted(option)
        }.onFailure {
            log.e { "Failed to vote for $pollBlueId: $it" }

        }
        return PollBlueVote.NotVoted
    }
}

