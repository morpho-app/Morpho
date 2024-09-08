package com.morpho.app.model.uidata

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import app.bsky.actor.Visibility
import com.atproto.label.LabelValue
import com.atproto.label.Severity
import com.morpho.app.model.bluesky.*
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Butterfly
import com.morpho.butterfly.Language
import com.morpho.butterfly.model.ReadOnlyList
import kotlinx.collections.immutable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lighthousegames.logging.logging

@Immutable
@Serializable
data class ContentHandling(
    val scope: LabelScope,
    val action: LabelAction,
    val source: LabelDescription,
    val id: String,
    @Contextual
    val icon: ImageVector,
)

sealed interface LabelDescription {
    val name: String
    val description: String

    sealed interface Block: LabelDescription
    data object Blocking: Block {
        override val name: String = "User Blocked"
        override val description: String = "You have blocked this user. You cannot view their content"

    }
    data object BlockedBy: Block {
        override val name: String = "User Blocking You"
        override val description: String = "This user has blocked you. You cannot view their content."
    }
    data class BlockList(
        val listName: String,
        val listUri: AtUri,
    ): Block {
        override val name: String = "User Blocked by $listName"
        override val description: String = "This user is on a block list you subscribe to. You cannot view their content."
    }
    data object OtherBlocked: Block {
        override val name: String = "Content Not Available"
        override val description: String = "This content is not available because one of the users involved has blocked the other."
    }

    sealed interface Muted: LabelDescription
    data class MuteList(
        val listName: String,
        val listUri: AtUri,
    ): Muted {
        override val name: String = "User Muted by $listName"
        override val description: String = "This user is on a mute list you subscribe to."
    }
    data object YouMuted: Muted {
        override val name: String = "Account Muted"
        override val description: String = "You have muted this user."
    }
    data class MutedWord(val word: String): Muted {
        override val name: String = "Post Hidden by Muted Word"
        override val description: String = "This post contains the word or tag \"$word\". You've chosen to hide it."
    }

    data class HiddenPost(val uri: AtUri): LabelDescription {
        override val name: String = "Post Hidden by You"
        override val description: String = "You have hidden this post."
    }

    data class Label(
        override val name: String,
        override val description: String,
        val severity: Severity,
    ): LabelDescription
}

sealed interface LabelSource {
    data object User: LabelSource
    data class List(
        val list: BskyList,
    ): LabelSource
    data class Labeler(
        val labeler: BskyLabelService,
    ): LabelSource
}

sealed interface LabelCause {
    val downgraded: Boolean
    val priority: Int
    val source: LabelSource
    data class Blocking(
        override val source: LabelSource,
        override val downgraded: Boolean,
    ): LabelCause {
        override val priority: Int = 3
    }
    data class BlockedBy(
        override val source: LabelSource,
        override val downgraded: Boolean,
    ): LabelCause {
        override val priority: Int = 4
    }

    data class BlockOther(
        override val source: LabelSource,
        override val downgraded: Boolean,
    ): LabelCause {
        override val priority: Int = 4
    }

    data class Label(
        override val source: LabelSource,
        val label: BskyLabel,
        val labelDef: InterpretedLabelDefinition,
        val target: LabelTarget,
        val setting: LabelSetting,
        val behaviour: ModBehaviour,
        val noOverride: Boolean,
        override val priority: Int,
        override val downgraded: Boolean,
    ): LabelCause {
        init {
            require(
                priority == 1 || priority == 2 || priority == 3 ||
                        priority == 5 || priority == 7 || priority == 8
            )
        }
    }

    data class Muted(
        override val source: LabelSource,
        override val downgraded: Boolean,
    ): LabelCause {
        override val priority: Int = 6
    }

    data class MutedWord(
        override val source: LabelSource,
        override val downgraded: Boolean,
    ): LabelCause {
        override val priority: Int = 6
    }

    data class Hidden(
        override val source: LabelSource,
        override val downgraded: Boolean,
    ): LabelCause {
        override val priority: Int = 6
    }

}



@Serializable
@Immutable
open class InterpretedLabelDefinition(
    val identifier: String,
    val configurable: Boolean,
    val severity: Severity,
    val whatToHide: LabelScope,
    val defaultSetting: LabelSetting?,
    @Contextual
    val flags: List<LabelValueDefFlag> = persistentListOf(),
    val behaviours: ModBehaviours,
    val localizedName: String = "",
    val localizedDescription: String = "",
    @Contextual
    val allDescriptions: ImmutableMap<Language, LocalizedLabelDescription> = persistentMapOf(),
) {
    companion object {

    }

    public fun toContentHandling(target: LabelTarget, icon: ImageVector? = null): ContentHandling {
        val action = behaviours.forScope(whatToHide, target).minOrNull() ?: when(defaultSetting) {
            LabelSetting.HIDE -> LabelAction.Blur
            LabelSetting.WARN -> LabelAction.Alert
            LabelSetting.IGNORE -> LabelAction.Inform
            null -> LabelAction.None
        }
        return ContentHandling(
            id = identifier,
            scope = whatToHide,
            action = action,
            source = LabelDescription.Label(
                name = localizedName,
                description = localizedDescription,
                severity = severity,
            ),
            icon = icon ?: when(severity) {
                Severity.ALERT -> Icons.Default.Warning
                Severity.NONE -> Icons.Default.Info
                Severity.INFORM -> Icons.Default.Info
            }
        )
    }
}

val LABELS: PersistentMap<LabelValue, InterpretedLabelDefinition> = persistentMapOf(
    LabelValue.HIDE to Hide,
    LabelValue.WARN to Warn,
    LabelValue.NO_UNAUTHENTICATED to NoUnauthed,
    LabelValue.PORN to Porn,
    LabelValue.SEXUAL to Sexual,
    LabelValue.NUDITY to Nudity,
    LabelValue.GRAPHIC_MEDIA to GraphicMedia,
)
data object Hide: InterpretedLabelDefinition(
    "!hide",
    false,
    Severity.ALERT,
    LabelScope.Content,
    LabelSetting.HIDE,
    persistentListOf(LabelValueDefFlag.NoSelf, LabelValueDefFlag.NoOverride),
    ModBehaviours(
        account = ModBehaviour(
            profileList = LabelAction.Blur,
            profileView = LabelAction.Blur,
            avatar = LabelAction.Blur,
            banner = LabelAction.Blur,
            displayName = LabelAction.Blur,
            contentList = LabelAction.Blur,
            contentView = LabelAction.Blur,
        ),
        profile = ModBehaviour(
            avatar = LabelAction.Blur,
            banner = LabelAction.Blur,
            displayName = LabelAction.Blur,
        ),
        content = ModBehaviour(
            contentList = LabelAction.Blur,
            contentView = LabelAction.Blur,
        ),
    ),
    localizedName = "Hide",
    localizedDescription = "Hide",
)

data object Warn: InterpretedLabelDefinition(
    "!warn",
    false,
    Severity.NONE,
    LabelScope.Content,
    LabelSetting.WARN,
    persistentListOf(LabelValueDefFlag.NoSelf),
    ModBehaviours(
        account = ModBehaviour(
            profileList = LabelAction.Blur,
            profileView = LabelAction.Blur,
            avatar = LabelAction.Blur,
            banner = LabelAction.Blur,
            displayName = LabelAction.Blur,
            contentList = LabelAction.Blur,
            contentView = LabelAction.Blur,
        ),
        profile = ModBehaviour(
            avatar = LabelAction.Blur,
            banner = LabelAction.Blur,
            displayName = LabelAction.Blur,
        ),
        content = ModBehaviour(
            contentList = LabelAction.Blur,
            contentView = LabelAction.Blur,
        ),
    ),
    localizedName = "Warn",
    localizedDescription = "Warn",
)

data object NoUnauthed: InterpretedLabelDefinition(
    "!no-unauthenticated",
    false,
    Severity.NONE,
    LabelScope.Content,
    LabelSetting.HIDE,
    persistentListOf(LabelValueDefFlag.NoOverride, LabelValueDefFlag.Unauthed),
    ModBehaviours(
        account = ModBehaviour(
            profileList = LabelAction.Blur,
            profileView = LabelAction.Blur,
            avatar = LabelAction.Blur,
            banner = LabelAction.Blur,
            displayName = LabelAction.Blur,
            contentList = LabelAction.Blur,
            contentView = LabelAction.Blur,
        ),
        profile = ModBehaviour(
            avatar = LabelAction.Blur,
            banner = LabelAction.Blur,
            displayName = LabelAction.Blur,
        ),
        content = ModBehaviour(
            contentList = LabelAction.Blur,
            contentView = LabelAction.Blur,
        ),
    ),
    localizedName = "No Unauthenticated",
    localizedDescription = "Do not show to unauthenticated users",
)

data object Porn: InterpretedLabelDefinition(
    "porn",
    true,
    Severity.NONE,
    LabelScope.Media,
    LabelSetting.HIDE,
    persistentListOf(LabelValueDefFlag.Adult),
    ModBehaviours(
        account = ModBehaviour(
            avatar = LabelAction.Blur,
            banner = LabelAction.Blur,
        ),
        profile = ModBehaviour(
            avatar = LabelAction.Blur,
            banner = LabelAction.Blur,
        ),
        content = ModBehaviour(
            contentMedia = LabelAction.Blur,
        ),
    ),
    localizedName = "Sexually Explicit",
    localizedDescription = "This content is sexually explicit",
)

data object Sexual: InterpretedLabelDefinition(
    "sexual",
    true,
    Severity.NONE,
    LabelScope.Media,
    LabelSetting.HIDE,
    persistentListOf(LabelValueDefFlag.Adult),
    ModBehaviours(
        account = ModBehaviour(
            avatar = LabelAction.Blur,
            banner = LabelAction.Blur,
        ),
        profile = ModBehaviour(
            avatar = LabelAction.Blur,
            banner = LabelAction.Blur,
        ),
        content = ModBehaviour(
            contentMedia = LabelAction.Blur,
        ),
    ),
    localizedName = "Suggestive",
    localizedDescription = "This content may be suggestive or sexual in nature",
)

data object Nudity: InterpretedLabelDefinition(
    "nudity",
    true,
    Severity.NONE,
    LabelScope.Media,
    LabelSetting.HIDE,
    persistentListOf(LabelValueDefFlag.Adult),
    ModBehaviours(
        account = ModBehaviour(
            avatar = LabelAction.Blur,
            banner = LabelAction.Blur,
        ),
        profile = ModBehaviour(
            avatar = LabelAction.Blur,
            banner = LabelAction.Blur,
        ),
        content = ModBehaviour(
            contentMedia = LabelAction.Blur,
        ),
    ),
    localizedName = "Nudity",
    localizedDescription = "This content contains nudity, artistic or otherwise",
)

data object GraphicMedia: InterpretedLabelDefinition(
    "graphic-media",
    true,
    Severity.NONE,
    LabelScope.Media,
    LabelSetting.HIDE,
    persistentListOf(LabelValueDefFlag.Adult),
    ModBehaviours(
        account = ModBehaviour(
            avatar = LabelAction.Blur,
            banner = LabelAction.Blur,
        ),
        profile = ModBehaviour(
            avatar = LabelAction.Blur,
            banner = LabelAction.Blur,
        ),
        content = ModBehaviour(
            contentMedia = LabelAction.Blur,
        ),
    ),
    localizedName = "Graphic Content",
    localizedDescription = "This content is graphic or violent in nature",
)


class ContentLabelService: KoinComponent {
    val api:Butterfly by inject()
    val settings: SettingsService by inject()

    val labelers = settings.labelers.stateIn(serviceScope, SharingStarted.Lazily, persistentListOf())
    val labelPrefs = settings.contentLabelPrefs.stateIn(serviceScope, SharingStarted.Lazily, persistentListOf())
    val mutedUsers = settings.mutedUsers.stateIn(serviceScope, SharingStarted.Lazily, persistentListOf())
    val mutedWords = settings.mutedWords.stateIn(serviceScope, SharingStarted.Lazily, persistentListOf())
    val hiddenPosts = settings.hiddenPosts.stateIn(serviceScope, SharingStarted.Lazily, persistentListOf())
    val showAdultContent = settings.showAdultContent.stateIn(serviceScope, SharingStarted.Lazily, false)
    val feedPrefs = settings.feedViewPrefs.stateIn(serviceScope, SharingStarted.Lazily, mapOf())
    val labelsToHide = labelPrefs.map { contentLabelPrefs ->
        contentLabelPrefs.fastFilter { it.visibility == Visibility.HIDE }
    }.stateIn(serviceScope, SharingStarted.Eagerly, persistentListOf())

    private val handlingCache = mutableMapOf<AtUri, ReadOnlyList<ContentHandling>>()
    private val definitionCache = mutableMapOf<String, InterpretedLabelDefinition>()

    companion object {
        val log = logging()
        val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    init {
        serviceScope.launch {
            while(!api.isLoggedIn()) {
                delay(100)
            }
            if (api.isLoggedIn()) {
                initDefinitionCache()
            }

        }

    }

    private fun initDefinitionCache() {
        val labelers = labelers.value
        log.verbose { "Labelers: $labelers" }
        val labelPrefs = labelPrefs.value
        log.verbose { "Label prefs: $labelPrefs" }
        val labelPrefMap = labelPrefs.associateBy { if (it.labelerDid == null) it.label else it.labelerDid.toString() }
        val labelerMap = labelers.associateBy { it.did.toString() }
        log.verbose { "Labeler map: $labelerMap" }
        val labelMap = labelerMap.mapValues { (id, labeler) ->
            val labelPref = labelPrefMap[id]
            if (labelPref != null) {
                val policy = labeler.policies.firstOrNull { it.identifier == labelPref.label }
                if (policy != null) {
                    Pair(
                        labeler.labels.first { it.value == policy.identifier },
                        policy.copy(defaultSetting = labelPref.visibility.toLabelSetting()),
                    )
                } else {
                    Pair(
                        labeler.labels.first { label ->
                            labeler.policies.fastAny { it.identifier == label.value } },
                        labeler.policies.first { def ->
                            labeler.labels.fastAny { it.value == def.identifier } },
                    )
                }
            } else {
                Pair(
                    labeler.labels.first { label ->
                        labeler.policies.fastAny { it.identifier == label.value } },
                    labeler.policies.first { def ->
                        labeler.labels.fastAny { it.value == def.identifier } },
                )
            }
        }
        val definitionMap = labelMap.mapValues { (id, pair) ->
            val (label, policy) = pair
            val name = label.value
            val flags = mutableListOf<LabelValueDefFlag>()
            var interpreted: InterpretedLabelDefinition? = null
            if (policy.adultOnly == true) {
                flags.add(LabelValueDefFlag.Adult)
            }
            when (label.getLabelValue()) {
                LabelValue.HIDE -> interpreted = Hide
                LabelValue.WARN -> interpreted = Warn
                LabelValue.NO_UNAUTHENTICATED -> interpreted = NoUnauthed
                LabelValue.PORN -> interpreted = Porn
                LabelValue.SEXUAL -> interpreted = Sexual
                LabelValue.NSFL -> interpreted = GraphicMedia
                LabelValue.GORE -> interpreted = GraphicMedia
                LabelValue.GRAPHIC_MEDIA -> interpreted = GraphicMedia
                else -> {}
            }

            if (interpreted == null) {
                val behaviours = when (policy.whatToHide) {
                    LabelScope.Content -> ModBehaviours(
                        account = ModBehaviour(
                            contentList = LabelAction.Blur,
                            contentView = LabelAction.Blur,
                        ),
                        profile = ModBehaviour(
                            contentList = LabelAction.Blur,
                            contentView = LabelAction.Blur,
                        ),
                        content = ModBehaviour(
                            contentList = LabelAction.Blur,
                            contentView = LabelAction.Blur,
                        ),
                    )
                    LabelScope.Media -> BlurAllMedia
                    LabelScope.None -> ModBehaviours(
                        NoopBehaviour,
                        NoopBehaviour,
                        NoopBehaviour,
                    )
                }
                interpreted = InterpretedLabelDefinition(
                    policy.identifier,
                    true,
                    policy.severity,
                    policy.whatToHide,
                    policy.defaultSetting,
                    flags.toImmutableList(),
                    behaviours,
                    localizedName = policy.localizedName,
                    localizedDescription = policy.localizedDescription,
                    allDescriptions = policy.allDescriptions,
                )
            }
            Pair(name, interpreted)
        }.values.toMap()
        definitionCache.putAll(definitionMap)
    }

    fun getContentHandlingForPost(post: BskyPost): List<ContentHandling> {
//        // TODO: Add some way to invalidate the cache
//        if (handlingCache.containsKey(post.uri)) {
//            return handlingCache[post.uri]!!
//        }
        val result = mutableListOf<ContentHandling>()
        val causes = mutableListOf<LabelCause>()
        val labels = post.labels
        if (hiddenPosts.value.contains(post.uri)) {
            causes.add(LabelCause.Hidden(LabelSource.User, false))
            result.add(Hide.toContentHandling(LabelTarget.Content))
            // Short circuit if the post is hidden, we shouldn't really get here
            // Generally it will be filtered out at the feed retrieval level
            return result.toImmutableList()
        }
        if (labels.isNotEmpty()) {
            log.verbose { "Post ${post.uri} has labels: ${labels.joinToString { it.value }}" }
            if (!showAdultContent.value) {
                val adultLabeler = labelPrefs.value.fastFilter { prefLabel ->
                    labels.fastAny { bskyLabel ->
                        prefLabel.label == bskyLabel.value &&
                            labelers.value.fastAny { it.policies.fastAny { policy ->
                                policy.adultOnly == true && policy.identifier == prefLabel.label
                            } }
                    }
                }
                val adultLabel = labels.firstOrNull { bskyLabel ->
                    val value = bskyLabel.getLabelValue()
                    value == LabelValue.GRAPHIC_MEDIA
                        || value == LabelValue.GORE
                        || value == LabelValue.NSFL
                        || value == LabelValue.PORN
                        || value == LabelValue.SEXUAL
                        || value == LabelValue.NUDITY
                        || adultLabeler.isNotEmpty()
                }
                log.debug { "Post ${post.uri} has adult label: $adultLabel" }
                when (adultLabel?.getLabelValue()) {
                    LabelValue.PORN -> causes.add(LabelCause.Label(
                        LabelSource.Labeler(labelers.value.firstOrNull { it.did == adultLabel.creator } ?: BlueskyHardcodedLabeler),
                        adultLabel,
                        Porn,
                        LabelTarget.Content,
                        LabelSetting.HIDE,
                        Porn.behaviours.content,
                        noOverride = true,
                        priority = 7,
                        downgraded = false,
                    ))
                    LabelValue.SEXUAL -> causes.add(LabelCause.Label(
                        LabelSource.Labeler(labelers.value.firstOrNull { it.did == adultLabel.creator } ?: BlueskyHardcodedLabeler),
                        adultLabel,
                        Sexual,
                        LabelTarget.Content,
                        LabelSetting.HIDE,
                        Sexual.behaviours.content,
                        noOverride = true,
                        priority = 7,
                        downgraded = false,
                    ))
                    LabelValue.NUDITY -> causes.add(LabelCause.Label(
                        LabelSource.Labeler(labelers.value.firstOrNull { it.did == adultLabel.creator } ?: BlueskyHardcodedLabeler),
                        adultLabel,
                        Nudity,
                        LabelTarget.Content,
                        LabelSetting.HIDE,
                        Nudity.behaviours.content,
                        noOverride = true,
                        priority = 7,
                        downgraded = false,
                    ))
                    LabelValue.GRAPHIC_MEDIA -> causes.add(LabelCause.Label(
                        LabelSource.Labeler(labelers.value.firstOrNull { it.did == adultLabel.creator } ?: BlueskyHardcodedLabeler),
                        adultLabel,
                        GraphicMedia,
                        LabelTarget.Content,
                        LabelSetting.HIDE,
                        GraphicMedia.behaviours.content,
                        noOverride = true,
                        priority = 8,
                        downgraded = false,
                    ))
                    LabelValue.NSFL -> causes.add(LabelCause.Label(
                        LabelSource.Labeler(labelers.value.firstOrNull { it.did == adultLabel.creator } ?: BlueskyHardcodedLabeler),
                        adultLabel,
                        GraphicMedia,
                        LabelTarget.Content,
                        LabelSetting.HIDE,
                        GraphicMedia.behaviours.content,
                        noOverride = true,
                        priority = 8,
                        downgraded = false,
                    ))
                    LabelValue.GORE -> causes.add(LabelCause.Label(
                        LabelSource.Labeler(labelers.value.firstOrNull { it.did == adultLabel.creator } ?: BlueskyHardcodedLabeler),
                        adultLabel,
                        GraphicMedia,
                        LabelTarget.Content,
                        LabelSetting.HIDE,
                        GraphicMedia.behaviours.content,
                        noOverride = true,
                        priority = 8,
                        downgraded = false,
                    ))
                    null -> {}
                    else -> {
                        adultLabeler.fastForEach { prefLabel ->
                            val labeler = labelers.value.firstOrNull { it.did == prefLabel.labelerDid }
                            val labelDef = labeler?.policies?.firstOrNull { it.identifier == prefLabel.label }
                            if (labeler != null && labelDef != null) {
                                val cached = definitionCache[prefLabel.label]
                                if (cached != null) {
                                    val cause = LabelCause.Label(
                                        LabelSource.Labeler(labeler),
                                        adultLabel,
                                        cached,
                                        LabelTarget.Content,
                                        prefLabel.visibility.toLabelSetting(),
                                        cached.behaviours.content,
                                        noOverride = false,
                                        priority = 7,
                                        downgraded = false,
                                    )
                                    causes.add(cause)
                                } else {
                                    val behaviours = when (labelDef.whatToHide) {
                                        LabelScope.Content -> ModBehaviours(
                                            account = ModBehaviour(
                                                contentList = LabelAction.Blur,
                                                contentView = LabelAction.Blur,
                                            ),
                                            profile = ModBehaviour(
                                                contentList = LabelAction.Blur,
                                                contentView = LabelAction.Blur,
                                            ),
                                            content = ModBehaviour(
                                                contentList = LabelAction.Blur,
                                                contentView = LabelAction.Blur,
                                            ),
                                        )
                                        LabelScope.Media -> BlurAllMedia
                                        LabelScope.None -> ModBehaviours(
                                            NoopBehaviour,
                                            NoopBehaviour,
                                            NoopBehaviour,
                                        )
                                    }
                                    val interpreted = InterpretedLabelDefinition(
                                        adultLabel.value,
                                        true,
                                        labelDef.severity,
                                        labelDef.whatToHide,
                                        labelDef.defaultSetting,
                                        persistentListOf(LabelValueDefFlag.Adult),
                                        behaviours,
                                        localizedName = labelDef.localizedName,
                                        localizedDescription = labelDef.localizedDescription,
                                    )
                                    val cause = LabelCause.Label(
                                        LabelSource.Labeler(labeler),
                                        adultLabel,
                                        interpreted,
                                        LabelTarget.Content,
                                        prefLabel.visibility.toLabelSetting(),
                                        interpreted.behaviours.content,
                                        noOverride = false,
                                        priority = 7,
                                        downgraded = false,
                                    )
                                    causes.add(cause)
                                    definitionCache[prefLabel.label] = interpreted
                                }
                            }

                        }
                    }

                }
            }
            val labelsWeCareAbout = labelPrefs.value.fastFilter { prefLabel ->
                labels.fastAny { it.value == prefLabel.label }
            }

            log.verbose { "Post ${post.uri} has labels we care about: ${labelsWeCareAbout.joinToString { it.label }}" }
            labelsWeCareAbout.fastForEach { prefLabel ->
                val cachedInterpretation = definitionCache[prefLabel.label]
                if (cachedInterpretation != null) {
                    log.verbose { "Post ${post.uri} has cached interpretation for ${prefLabel.label}" }
                    val cause = LabelCause.Label(
                        LabelSource.Labeler(labelers.value.firstOrNull { it.did == prefLabel.labelerDid }!!),
                        labels.first { it.value == prefLabel.label },
                        cachedInterpretation,
                        LabelTarget.Content,
                        prefLabel.visibility.toLabelSetting(),
                        cachedInterpretation.behaviours.content,
                        noOverride = false,
                        priority = 5,
                        downgraded = false,
                    )
                    causes.add(cause)
                } else {
                    val labeler = labelers.value.firstOrNull { it.did == prefLabel.labelerDid }
                    val labelDef = labeler?.policies?.firstOrNull { it.identifier == prefLabel.label }
                    if (labeler != null && labelDef != null) {
                        val behaviours = when (labelDef.whatToHide) {
                            LabelScope.Content -> ModBehaviours(
                                account = ModBehaviour(
                                    contentList = LabelAction.Blur,
                                    contentView = LabelAction.Blur,
                                ),
                                profile = ModBehaviour(
                                    contentList = LabelAction.Blur,
                                    contentView = LabelAction.Blur,
                                ),
                                content = ModBehaviour(
                                    contentList = LabelAction.Blur,
                                    contentView = LabelAction.Blur,
                                ),
                            )
                            LabelScope.Media -> BlurAllMedia
                            LabelScope.None -> ModBehaviours(
                                NoopBehaviour,
                                NoopBehaviour,
                                NoopBehaviour,
                            )
                        }
                        val interpreted = InterpretedLabelDefinition(
                            labelDef.identifier,
                            true,
                            labelDef.severity,
                            labelDef.whatToHide,
                            labelDef.defaultSetting,
                            persistentListOf(LabelValueDefFlag.Adult),
                            behaviours,
                            localizedName = labelDef.localizedName,
                            localizedDescription = labelDef.localizedDescription,
                        )
                        val cause = LabelCause.Label(
                            LabelSource.Labeler(labeler),
                            labels.first { it.value == prefLabel.label },
                            interpreted,
                            LabelTarget.Content,
                            prefLabel.visibility.toLabelSetting(),
                            interpreted.behaviours.content,
                            noOverride = false,
                            priority = 5,
                            downgraded = false,
                        )
                        causes.add(cause)
                        definitionCache[prefLabel.label] = interpreted
                    }
                }
            }
        }
        causes.sortByDescending { it.priority }
        causes.fastForEach { cause ->
            // TODO: handle stuff from lists and so on
            when (cause) {
                is LabelCause.Blocking -> {
                    result.add(ContentHandling(
                        scope = LabelScope.Content,
                        action = LabelAction.Blur,
                        source = LabelDescription.Blocking,
                        id = "blocking",
                        icon = Icons.Default.Info,
                    ))
                }
                is LabelCause.BlockedBy -> {
                    result.add(ContentHandling(
                        scope = LabelScope.Content,
                        action = LabelAction.Blur,
                        source = LabelDescription.BlockedBy,
                        id = "blocked-by",
                        icon = Icons.Default.Info,
                    ))
                }
                is LabelCause.BlockOther -> {
                    result.add(ContentHandling(
                        scope = LabelScope.Content,
                        action = LabelAction.Blur,
                        source = LabelDescription.OtherBlocked,
                        id = "blocked-other",
                        icon = Icons.Default.Info,
                    ))
                }
                is LabelCause.Muted -> {

                    result.add(ContentHandling(
                        scope = LabelScope.Content,
                        action = LabelAction.Blur,
                        source = LabelDescription.YouMuted,
                        id = "muted",
                        icon = Icons.Default.Info,
                    ))
                }
                is LabelCause.MutedWord -> {
                    result.add(
                        ContentHandling(
                            scope = LabelScope.Content,
                            action = LabelAction.Blur,
                            source = LabelDescription.MutedWord("Some word"),
                            id = "muted-word",
                            icon = Icons.Default.Info,
                        )
                    )
                }
                is LabelCause.Label -> {
                    val handling = cause.labelDef.toContentHandling(cause.target)
                    result.add(handling)
                }
                is LabelCause.Hidden -> {
                    result.add(Hide.toContentHandling(LabelTarget.Content))
                }
            }
        }

        log.verbose { "Post ${post.uri} has handling: \n$result" }
        return result.toList()
    }

}