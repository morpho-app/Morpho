package com.morpho.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import app.bsky.actor.MuteTargetGroup
import app.bsky.actor.MutedWord
import com.morpho.app.data.MorphoAgent
import com.morpho.app.model.uidata.Moment
import com.morpho.app.ui.elements.WrappedColumn
import com.morpho.app.ui.elements.WrappedLazyColumn
import com.morpho.app.util.getFormattedDateTimeSince
import com.morpho.butterfly.model.Timestamp
import com.morpho.butterfly.mutedWordContent
import com.morpho.butterfly.mutedWordTag
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.koin.compose.getKoin
import kotlin.time.Duration

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MutedWordsSettings(
    agent: MorphoAgent = getKoin().get(),
    scope: CoroutineScope = rememberCoroutineScope(),
    modifier: Modifier = Modifier,
) {
    var word: TextFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    val focusManager = LocalFocusManager.current
    var duration by remember { mutableStateOf(MuteDuration.FOREVER) }
    var target by remember { mutableStateOf(MuteTargetGroup.ALL) }
    var targetType by remember { mutableStateOf(mutedWordContent) }
    val mutedWords = agent.prefs.modPrefs.mutedWords.toMutableStateList()
    WrappedLazyColumn (
        modifier = modifier.fillMaxWidth()
    ) {
        val verticalPadding = 8.dp
        item {
            WrappedColumn(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Edit muted words and tags",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = verticalPadding)
                )
                Text(
                    "Posts can be muted based on their text, tags, or both. " +
                            "Avoid muting very common words, phrases, or tags, "
                            + "as this can prevent you from seeing essentially any posts.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = verticalPadding, horizontal = 8.dp)
                )
                OutlinedTextField(
                    value = word,
                    placeholder = { Text(text = "Enter a word or tag to mute") },
                    onValueChange = { text: TextFieldValue ->
                        word = text
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.padding(vertical = verticalPadding, horizontal = 8.dp).fillMaxWidth()
                )
                Text(
                    "Duration",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = verticalPadding)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = verticalPadding)
                ) {
                    MutedWordDurationSelector(
                        initialDuration = duration,
                        text = "Forever",
                        value = MuteDuration.FOREVER,
                        onSelected = { duration = it }
                    )
                    MutedWordDurationSelector(
                        initialDuration = duration,
                        text = "1 day",
                        value = MuteDuration.ONE_DAY,
                        onSelected = { duration = it }
                    )
                    MutedWordDurationSelector(
                        initialDuration = duration,
                        text = "1 week",
                        value = MuteDuration.ONE_WEEK,
                        onSelected = { duration = it }
                    )
                    MutedWordDurationSelector(
                        initialDuration = duration,
                        text = "1 month",
                        value = MuteDuration.ONE_MONTH,
                        onSelected = { duration = it }
                    )

                }
                Text(
                    "Mute in:",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = verticalPadding)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = verticalPadding)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        tonalElevation = 8.dp,
                        //color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )  {
                            RadioButton(
                                selected = targetType == mutedWordContent,
                                onClick = {
                                    targetType = mutedWordContent
                                }
                            )
                            Text(
                                text = "Text and Tags",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        tonalElevation = 8.dp,
                        //color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            RadioButton(
                                selected = targetType == mutedWordTag,
                                onClick = {
                                    targetType = mutedWordTag
                                }
                            )
                            Text(
                                text = "Tags Only",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                }
                Text(
                    "Additional options:",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = verticalPadding)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = verticalPadding)
                ){
                    Switch(
                        checked = target == MuteTargetGroup.EXCLUDE_FOLLOWING,
                        onCheckedChange = {
                            target = if(it) MuteTargetGroup.EXCLUDE_FOLLOWING else MuteTargetGroup.ALL
                        }
                    )
                    Text(
                        text = "Exclude users that you follow",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
                FilledTonalButton(
                    enabled = word.text.isNotEmpty(),
                    onClick = {
                        val now = Clock.System.now()
                        val expiresAt: Timestamp? = if(duration == MuteDuration.FOREVER) null
                        else now.plus(duration.duration)
                        val newWord = MutedWord(
                            value = word.text,
                            targets = if(targetType == mutedWordContent) persistentListOf(
                                mutedWordContent,
                                mutedWordTag
                            ) else persistentListOf(mutedWordTag),
                            actorTarget = target,
                            expiresAt = expiresAt?.toString(),
                        )
                        mutedWords.add(newWord)
                        scope.launch {
                            agent.updateMutedWord(newWord)
                        }
                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(verticalPadding).fillMaxWidth()
                ) {
                    Text(
                        text = "Add",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                    )
                }
                HorizontalDivider(Modifier.fillMaxWidth().padding(vertical = verticalPadding))
                Text(
                    "Words you have muted",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = verticalPadding)
                )
            }
        }
        if(mutedWords.isEmpty()) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    tonalElevation = 8.dp,
                    //color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
                    modifier = Modifier.fillMaxWidth().padding( verticalPadding),
                ) {
                    Text(
                        text = "No muted words",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        } else {
            items(mutedWords) {
                MutedWordListItem(
                    word = it,
                    onRemoveClicked = {
                        mutedWords.remove(it)
                        scope.launch {
                            agent.removeMutedWord(it)

                        }
                    }
                )
            }

        }


    }
}

@Composable
fun MutedWordListItem(
    word: MutedWord,
    onRemoveClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        tonalElevation = 8.dp,
        //color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
        modifier = modifier.fillMaxWidth().padding(4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(4.dp)
        )  {
            Column {
                val valueAndTargets = buildAnnotatedString {
                    pushStyle(
                        SpanStyle(fontWeight = FontWeight.Bold)
                    )
                    append(word.value)
                    pop()
                    append(" in ")
                    pushStyle(
                        SpanStyle(fontWeight = FontWeight.SemiBold)
                    )
                    val targetsString = if(word.targets.contains(mutedWordContent) && word.targets.contains(mutedWordTag)) {
                        "Text and Tags"
                    } else if(word.targets.contains(mutedWordContent)) {
                        "Text"
                    } else if(word.targets.contains(mutedWordTag)) {
                        "Tags"
                    } else word.targets.joinToString(", ") { it.mutedWordTarget }
                    append(targetsString)
                    pop()

                    toAnnotatedString()
                }
                Text(
                    text = valueAndTargets,
                    style = MaterialTheme.typography.bodyMedium,
                )
                val expiry = word.expiresAt?.let {Moment( Instant.parse(it))  }
                val timeToExpire = if(expiry != null) {
                    getFormattedDateTimeSince(expiry)
                } else null
                val expiryAndExludes = buildAnnotatedString {
                    if(timeToExpire != null) {
                        append("Expires in ")
                        append(timeToExpire)
                        pushStyle(
                            SpanStyle(fontWeight = FontWeight.SemiBold)
                        )
                        append(" - ")
                        pop()
                    }
                    if(word.actorTarget != MuteTargetGroup.ALL) {
                        append("Excludes users that you follow")
                    }
                    toAnnotatedString()
                }
                Text(
                    text = expiryAndExludes,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )

            }
            OutlinedIconButton(
                onClick = onRemoveClicked,
                modifier = Modifier.padding(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                )
            }
        }
    }
}

@Composable
fun MutedWordDurationSelector(
    initialDuration: MuteDuration,
    text: String,
    value: MuteDuration,
    onSelected: (MuteDuration) -> Unit,
    modifier: Modifier = Modifier,
) {
    var duration by remember { mutableStateOf(initialDuration) }
    Surface(
        shape = MaterialTheme.shapes.small,
        tonalElevation = 8.dp,
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
        modifier = modifier.padding(4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        )  {
            RadioButton(
                selected = duration == value,
                onClick = {
                    duration = value
                    onSelected(value)
                }
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = 12.dp)
            )
        }
    }
}

enum class MuteDuration(val duration: Duration, val text: String) {
    FOREVER(Duration.INFINITE, "Forever"),
    ONE_DAY(Duration.parse("24h"), "24 hours"),
    ONE_WEEK(Duration.parse("7d"), "7 days"),
    ONE_MONTH(Duration.parse("30d"), "30 days"),
}

