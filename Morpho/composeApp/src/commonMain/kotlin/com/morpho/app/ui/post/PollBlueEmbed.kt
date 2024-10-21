package com.morpho.app.ui.post

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastFold
import androidx.compose.ui.util.fastForEach
import com.morpho.app.data.PollBlueId
import com.morpho.app.data.PollBlueService
import com.morpho.app.data.PollBlueVote
import com.morpho.app.data.stripPollOptionCharacters
import com.morpho.app.model.bluesky.BskyFacet
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.FacetType
import com.morpho.app.ui.elements.RichTextElement
import com.morpho.app.util.openBrowser
import com.morpho.app.util.utf8Slice
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.ContentHandling
import com.morpho.butterfly.Uri
import kotlinx.coroutines.launch
import org.koin.compose.getKoin
import kotlin.math.roundToInt

fun parsePollBlueUrl(url: Uri): PollBlueId {
    val urlParts = url.toString().split("/")
    return urlParts[urlParts.size - 2]
}

@Composable
fun ColumnScope.PollBlueOption(
    optionChosen: PollBlueVote,
    text: String,
    pollFacet: BskyFacet,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val optionIndex = remember { when (val type = pollFacet.facetType.first()) {
         is FacetType.PollBlueOption -> type.number
         else -> throw IllegalArgumentException("Expected PollBlueOption, got $type")
    } }

    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier//.fillMaxWidth()
            .clickable { onClick(optionIndex) }
    ) {
        Icon(
            if (optionChosen is PollBlueVote.Voted && optionChosen.chosen == optionIndex) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
            contentDescription = "$optionIndex",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text,
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.alignByBaseline()
        )
    }
}

fun findFirstFacet(facets: List<BskyFacet>): Int {
    return facets.fastFold(Int.MAX_VALUE) { acc, facet ->
        // Can maybe remove the inner fold, since there's mostly only one type per facet
        // and Poll.Blue.Option separates link from poll option facet
        val tempAcc = facet.facetType.fastFold(acc) { accInner, type ->
            when (type) {
                is FacetType.PollBlueOption -> minOf(accInner, facet.start)
                else -> accInner
            }
        }
        if (tempAcc < acc) tempAcc else acc
    }
}

fun findLastFacet(facets: List<BskyFacet>): Int {
    return facets.fastFold(0) { acc, facet ->
        // Can maybe remove the inner fold, since there's mostly only one type per facet
        // and Poll.Blue.Option separates link from poll option facet
        val tempAcc = facet.facetType.fastFold(acc) { accInner, type ->
            when (type) {
                is FacetType.PollBlueOption -> maxOf(accInner, facet.start)
                else -> accInner
            }
        }
        if (tempAcc > acc) tempAcc else acc
    }
}

@Composable
fun PollBluePost(
    text: String,
    facets: List<BskyFacet>,
    modifier: Modifier = Modifier,
    onItemClicked: () -> Unit = {},
    onProfileClicked: (AtIdentifier) -> Unit = {},
    getContentHandling: (BskyPost) -> List<ContentHandling> = { listOf() }

) {
    val optionsStart = remember { findFirstFacet(facets) }
    val questionText = remember { text.utf8Slice(0 until optionsStart) }
    val pollFacets = remember { facets.filter { it.facetType.first() is FacetType.PollBlueOption } }
    val pollLinkFacets = remember { facets.filter {
        it.facetType.first() is FacetType.ExternalLink
                && pollFacets.fastAny { pollFacet -> pollFacet.start == it.start }
    } }
    val questionFacets = remember { facets.filter {
        it.end <= optionsStart
                && it.facetType.first() !is FacetType.PollBlueOption
    } }
    var showResults by remember { mutableStateOf(false) }
    var pollResults by remember { mutableStateOf(emptyList<Pair<String, Int>>()) }
    val pollBlueService = getKoin().get<PollBlueService>()
    val pollId: PollBlueId = remember { parsePollBlueUrl(
        pollLinkFacets.first().facetType.first().let { type ->
            if (type is FacetType.ExternalLink) type.uri else
                throw IllegalArgumentException("Expected ExternalLink, got $type")
        }
    ) }
    var optionChosen by remember { mutableStateOf(pollBlueService.lookupPollBlueVote(pollId)) }
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    DisableSelection {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = modifier.fillMaxWidth()
        ) {
            RichTextElement(
                text = stripPollOptionCharacters(questionText).trimEnd(),
                facets = questionFacets,
                onClick = { facetTypes ->
                    if (facetTypes.isEmpty()) {
                        onItemClicked()
                        return@RichTextElement
                    }
                    facetTypes.fastForEach {
                        when(it) {
                            is FacetType.ExternalLink -> {
                                openBrowser(it.uri.uri, uriHandler)
                            }
                            is FacetType.Tag -> {}
                            is FacetType.UserDidMention -> {
                                onProfileClicked(it.did)
                            }
                            is FacetType.UserHandleMention -> {
                                onProfileClicked(it.handle)
                            }
                            else -> {}
                        }
                    }
                },
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (!showResults && optionChosen is PollBlueVote.NotVoted) {
                pollFacets.forEach { pollFacet ->
                    PollBlueOption(
                        optionChosen = optionChosen,
                        text = stripPollOptionCharacters(text.utf8Slice(pollFacet.start until pollFacet.end)),
                        pollFacet = pollFacet,
                        onClick = { choice ->
                            scope.launch {
                                optionChosen = pollBlueService.vote(pollId, choice).also {
                                    scope.launch {
                                        pollResults = pollBlueService.getPollBlueResults(pollId)
                                        showResults = pollResults.isNotEmpty()
                                        println("Poll results: $pollResults")
                                    }
                                }
                            }
                        }
                    )
                }
            } else {
                PollBlueResults(
                    optionChosen = optionChosen,
                    results = pollResults
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (optionChosen is PollBlueVote.NotVoted) {
                    TextButton(
                        onClick = {
                            if(!showResults) {
                                // grab results from cache if available
                                // makes the UI feel faster
                                pollResults = pollBlueService.getCachedResults(pollId) ?: pollResults
                                if(pollResults.isNotEmpty()) showResults = true
                                scope.launch {
                                    pollResults = pollBlueService.getPollBlueResults(pollId)
                                    showResults = pollResults.isNotEmpty()
                                    println("Poll results: $pollResults")
                                }
                            } else {
                                showResults = false
                            }

                        },
                        contentPadding = PaddingValues(vertical = 0.dp, horizontal = 10.dp),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            if(showResults) "Hide Results" else "Show Results",
                            color = MaterialTheme.colorScheme.secondary,

                        )
                    }
                }
                Text(
                    "Made with @poll.blue",
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                        .clickable { openBrowser("https://poll.blue/post", uriHandler) }
                )
            }
        }
    }

}

@Composable
fun PollBlueResults(
    optionChosen: PollBlueVote,
    results: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {

    Column(modifier = Modifier.fillMaxWidth().padding( vertical = 4.dp, horizontal = 8.dp)) {
        val totalVotes =  results.sumOf { it.second }
        Text(
            "$totalVotes total votes",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.End).padding(bottom = 4.dp)
        )
        results.forEachIndexed { i, (text, votes) ->
            println("Option '$text' got $votes votes")
            PollBlueResultsOption(
                optionChosen = if (optionChosen is PollBlueVote.Voted && optionChosen.chosen == i + 1)
                    optionChosen else PollBlueVote.NotVoted,
                text = text,
                votes = votes,
                totalVotes = totalVotes
            )
        }

    }

}

@Composable
fun PollBlueResultsOption(
    optionChosen: PollBlueVote,
    text: String,
    votes: Int,
    totalVotes: Int,
    modifier: Modifier = Modifier
) {
    val fillColour = MaterialTheme.colorScheme.secondary
    val voteFraction = if (totalVotes == 0) 0f else votes.toFloat() / totalVotes.toFloat()
    OutlinedCard(
        border = BorderStroke(0.dp, Color.Transparent),
        shape = MaterialTheme.shapes.small,
        modifier = modifier.fillMaxWidth().padding(4.dp)

    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                .drawWithContent {
                    drawRoundRect(
                        cornerRadius = CornerRadius(8.dp.toPx()),
                        color = fillColour,
                        size = size.copy(width = size.width * voteFraction)
                    )
                    drawContent()
                },
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    if (optionChosen is PollBlueVote.Voted)
                        Icons.Default.RadioButtonChecked
                    else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "$votes",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alignByBaseline().padding(vertical = 4.dp)
                )

            }
            val percentWhole = (voteFraction * 100f).toInt()
            val percentFraction = ((voteFraction * 100f - percentWhole.toFloat()) * 10).roundToInt()
            Text(
                "${percentWhole}.${percentFraction}% ($votes)",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alignByBaseline()
                    .padding(vertical = 4.dp)
                    .padding(end = 8.dp)
            )
        }
    }
}