package com.morpho.app.model.uidata

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import app.bsky.actor.ContentLabelPref
import app.bsky.labeler.GetServicesQuery
import app.bsky.labeler.GetServicesResponseViewUnion
import com.atproto.label.LabelValue
import com.atproto.label.Severity
import com.morpho.app.data.PreferencesRepository
import com.morpho.app.model.bluesky.*
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Butterfly
import com.morpho.butterfly.Language
import kotlinx.collections.immutable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lighthousegames.logging.logging

@Immutable
@Serializable
data class ContentHandling(
    val scope: LabelScope,
    val source: LabelDescription,
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
    val flags: ImmutableList<LabelValueDefFlag> = persistentListOf(),
    val behaviours: ModBehaviours,
    val localizedName: String = "",
    val localizedDescription: String = "",
    @Contextual
    val allDescriptions: ImmutableMap<Language, LocalizedLabelDescription> = persistentMapOf(),
) {
    companion object {

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
    )
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
    )
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
    )
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
    )
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
    )
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
    )
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
    )
)


class ContentLabelService: KoinComponent {
    val api:Butterfly by inject()
    val preferences: PreferencesRepository by inject()

    val labelPrefs: MutableStateFlow<List<ContentLabelPref>> = MutableStateFlow(listOf())
    val labelers: MutableStateFlow<List<BskyLabelService>> = MutableStateFlow(listOf())

    companion object {
        val log = logging()
        val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    init {
        serviceScope.launch {
            while(api.id == null) {
                delay(100)
            }
            if (api.id != null) {
                preferences.userPrefs(api.id!!).map { prefs ->
                    labelPrefs.update { prefs?.preferences?.contentLabelPrefs ?: emptyList() }
                    val labelerProfiles = prefs?.preferences?.labelers?.toImmutableList()
                        ?.let { GetServicesQuery(it) }?.let {
                            api.api.getServices(it)
                                .map { resp ->
                                    resp.views.map { service ->
                                        when(service) {
                                            is GetServicesResponseViewUnion.LabelerView ->
                                                service.value.toLabelService()
                                            is GetServicesResponseViewUnion.LabelerViewDetailed ->
                                                service.value.toLabelService()
                                        }
                                    }
                                }.getOrNull()
                        } ?: emptyList()
                    labelers.update { labelerProfiles }
                }
            }

        }

    }


}