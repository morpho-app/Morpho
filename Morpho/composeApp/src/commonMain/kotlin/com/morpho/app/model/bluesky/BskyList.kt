package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import app.bsky.graph.ListType
import app.bsky.graph.ListView
import com.morpho.app.model.uidata.Moment
import com.morpho.app.util.mapImmutable
import com.morpho.butterfly.*
import com.morpho.butterfly.model.ReadOnlyList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class BskyList(
    val uri: AtUri,
    val cid: Cid,
    val creator: Profile,
    val name: String,
    val purpose: ListType,
    val description: String? = null,
    val descriptionFacets: List<BskyFacet> = listOf(),
    val avatar: String? = null,
    val viewerMuted: Boolean,
    val viewerBlocked: AtUri? = null,
    val indexedAt: Moment,
    val labels: List<BskyLabel> = listOf(),
    val listItems: List<Profile> =  listOf(),
) {
    override operator fun equals(other: Any?) : Boolean {
        return when(other) {
            null -> false
            is Cid -> other == cid
            is AtUri -> other == uri
            else -> other.hashCode() == this.hashCode()
        }
    }

    override fun hashCode(): Int {
        var result = uri.hashCode()
        result = 31 * result + cid.hashCode()
        result = 31 * result + creator.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + purpose.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + descriptionFacets.hashCode()
        result = 31 * result + (avatar?.hashCode() ?: 0)
        result = 31 * result + viewerMuted.hashCode()
        result = 31 * result + (viewerBlocked?.hashCode() ?: 0)
        result = 31 * result + labels.hashCode()
        result = 31 * result + listItems.hashCode()
        return result
    }

    operator fun contains(other: Any?) : Boolean {
        return listItems.contains(other)
    }
}

fun ReadOnlyList<Profile>.contains(other: Any?): Boolean {
    return when(other) {
        null            -> false
        is Did          -> this.any { it.did == other }
        is Handle       -> this.any { it.handle == other }
        is AtIdentifier -> {
            this.any { it.did.did == other.toString() } ||
            this.any { it.handle.handle == other.toString() }
        }
        is Profile -> this.any { it.did == other.did }
        else -> false
    }
}

fun ListView.toList(): BskyList {
    return BskyList(
        uri = this.uri,
        cid = this.cid,
        creator = this.creator.toProfile(),
        name = this.name,
        purpose = this.purpose,
        description = this.description,
        descriptionFacets = this.descriptionFacets.mapImmutable { it.toBskyFacet() },
        avatar = this.avatar,
        viewerMuted = this.viewer?.muted ?: false,
        viewerBlocked = this.viewer?.blocked,
        indexedAt = Moment(this.indexedAt),
        labels = this.labels.mapImmutable { it.toLabel() },
        listItems = this.items.map { it.toProfile() }.toImmutableList()
    )
}