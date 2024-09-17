package com.morpho.app.data

import app.bsky.actor.MuteTargetGroup
import app.bsky.actor.MutedWord
import app.bsky.actor.Visibility
import app.bsky.labeler.LabelerViewDetailed
import com.atproto.label.Blurs
import com.atproto.label.Severity
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.MorphoDataItem
import com.morpho.app.model.bluesky.toAtProtoLabel
import com.morpho.app.model.bluesky.toListVewBasic
import com.morpho.butterfly.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lighthousegames.logging.logging

class ContentLabelService: KoinComponent {
    val agent: MorphoAgent by inject()
    val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    companion object {
        val log = logging("ContentLabelService")
    }

    val modPrefs: ModerationPreferences
        get() = agent.prefs.modPrefs

    val hiddenPosts: List<AtUri>
        get() = modPrefs.hiddenPosts

    val mutedWords: List<MutedWord>
        get() = modPrefs.mutedWords


    val labelers: Map<LabelerID, Map<LabelValueID, Visibility>>
        get() = modPrefs.labelers

    val labels: Map<LabelValueID, Visibility>
        get() = modPrefs.labels

    var labelDefinitions: Map<LabelerID, Map<LabelValueID, InterpretedLabelDefinition>> = emptyMap()
        private set

    var labelerDetails: Map<LabelerID, LabelerViewDetailed> = emptyMap()
        private set

    init {
        serviceScope.launch {
            agent.getLabelDefinitions(modPrefs)
            agent.getLabelersDetailed(labelers.keys.map { Did(it) })
        }

    }

    fun shouldHideItem(item: MorphoDataItem.FeedItem): Boolean {
        return when (item) {
            is MorphoDataItem.Post -> {
                item.post.author.mutedByMe
                    || item.post.author.blocking
                    || item.post.author.blockedBy
                    || hiddenPosts.any { uri -> item.containsUri(uri) }
                    || mutedWords.any {
                        item.post.text.contains(it.value, ignoreCase = true)
                    } || if(!modPrefs.adultContentEnabled) {
                            val adultLabels = item.post.labels.filter { label ->
                                labelDefinitions[label.creator.did]?.get(label.value)?.flags
                                    ?.contains(LabelValueDefFlag.Adult) == true
                            }
                            adultLabels.isNotEmpty()
                        } else {
                            item.post.labels.any { label ->
                                labels[label.value] == Visibility.HIDE
                            }
                        }
            }
            is MorphoDataItem.Thread -> {
                item.thread.anyMutedOrBlocked()
                    || hiddenPosts.any { uri -> item.containsUri(uri) }
                    || mutedWords.any {
                        item.thread.containsWord(it.value)
                    } || if(!modPrefs.adultContentEnabled) {
                        val adultLabels = item.thread.getLabels().filter { label ->
                            labelDefinitions[label.creator.did]?.get(label.value)?.flags
                                ?.contains(LabelValueDefFlag.Adult) == true
                        }
                        adultLabels.isNotEmpty()
                    } else {
                        item.thread.getLabels().any { label ->
                            labels[label.value] == Visibility.HIDE
                        }
                    }
            }
        }
    }

    fun getContentHandlingForPost(post: BskyPost): List<Pair<ContentHandling, LabelCause>> {
        val result = mutableListOf<Pair<ContentHandling, LabelCause>>()
        val postLabels = post.labels

        if(post.author.mutedByMe) {
            result.add(ContentHandling(
                scope = Blurs.CONTENT,
                action = LabelAction.Blur,
                source = LabelDescription.YouMuted,
                id = "muted",
                icon = LabelIcon.EyeSlash(labelerAvatar = null),
            ) to LabelCause.Muted(LabelSource.User, false))
        }
        if(post.author.mutedByList != null) {
            val list = post.author.mutedByList!!
            result.add(ContentHandling(
                scope = Blurs.CONTENT,
                action = LabelAction.Blur,
                source = LabelDescription.MuteList(
                    list.name,
                    list.uri,
                ),
                id = "muted-word",
                icon = LabelIcon.EyeSlash( labelerAvatar = list.avatar),
            ) to LabelCause.Muted(LabelSource.List(list.toListVewBasic()), false))
        }
        val anyMutedWords = mutedWords.filter { post.text.contains(it.value, ignoreCase = true) }
        if(anyMutedWords.isNotEmpty()) anyMutedWords.forEach { word ->
            if(!word.targets.contains(MutedWordTarget("content"))) return@forEach
            if(word.actorTarget == MuteTargetGroup.EXCLUDE_FOLLOWING && post.author.followedByMe) return@forEach
            result.add(ContentHandling(
                scope = Blurs.CONTENT,
                action = LabelAction.Blur,
                source = LabelDescription.MutedWord(word.value),
                id = "muted-word",
                icon = LabelIcon.EyeSlash(),
            ) to LabelCause.MutedWord(LabelSource.User, false))
        }


        if (postLabels.isNotEmpty()) {
            log.verbose { "Post ${post.uri} has labels: ${postLabels.joinToString { it.value }}" }
            // Adult content hiding if someone doesn't have it enabled is handled earlier,
            // before rendering starts, as is Visibility.HIDE
            // so we don't need to worry about it here
            val relevantLabels = labels.filter { prefLabel ->
                (prefLabel.value == Visibility.WARN || prefLabel.value == Visibility.HIDE)
                        && postLabels.any { it.value == it.value } }.toList()
                .sortedBy { it.second.ordering }
            val filteredPostLabels = postLabels.filter { label ->
                relevantLabels.any { label.value == it.first }
            }

            val possibleCauses = filteredPostLabels.mapNotNull { label ->
                labelDefinitions[label.creator.did]?.get(label.value)?.let { labelDef ->
                    val localizedDefString = labelDef.allDescriptions.firstOrNull {
                        it.lang == agent.myLanguage
                    } ?: labelDef.allDescriptions.firstOrNull { it.lang.tag == "en" }
                    val localLabelDef = labelDef.copy(
                        localizedName = localizedDefString?.name ?: labelDef.localizedName,
                        localizedDescription = localizedDefString?.description
                            ?: labelDef.localizedDescription,
                    )

                    LabelCause.Label(
                        LabelSource.Labeler(labelerDetails[label.creator.did]!!),
                        label.toAtProtoLabel(),
                        localLabelDef,
                        localLabelDef.whatToHide,
                        labels[label.value] ?: labelDef.defaultSetting ?: Visibility.IGNORE,
                        localLabelDef.behaviours.content,
                        noOverride = !localLabelDef.configurable,
                        priority = when (localLabelDef.severity) {
                            Severity.INFORM -> 5
                            Severity.ALERT -> 1
                            Severity.NONE -> 8
                        },
                        downgraded = false,
                    ) to localLabelDef.toContentHandling(
                        LabelTarget.Content,
                        avatar = labelerDetails[label.creator.did]?.creator?.avatar
                    )
                }
            }.sortedBy{ it.first.priority }
            possibleCauses.forEach { (cause, handling) ->
                result.add(handling to cause)
            }
        }

        log.verbose { "Post ${post.uri} has handling: \n$result" }
        return result.toList()
    }

}