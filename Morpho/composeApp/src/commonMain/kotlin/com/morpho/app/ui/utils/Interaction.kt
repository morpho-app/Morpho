package com.morpho.app.ui.utils

import androidx.compose.ui.platform.UriHandler
import cafe.adriel.voyager.navigator.Navigator
import com.morpho.app.model.bluesky.FacetType
import com.morpho.app.screens.base.tabbed.ProfileTab
import com.morpho.app.screens.base.tabbed.ThreadTab
import com.morpho.app.util.openBrowser
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Did
import com.morpho.butterfly.Uri

fun onProfileClickedImmediate(
    actor: AtIdentifier,
    navigator: Navigator,
) {
    if(actor is Did) navigator.push(ProfileTab(actor))
}

fun onPostClickedImmediate(
    uri: AtUri,
    navigator: Navigator,
) {
    navigator.push(ThreadTab(uri))
}
typealias OnPostClicked = (AtUri) -> Unit
typealias OnFacetClicked = (FacetType) -> Unit
typealias OnLinkClicked = (Uri) -> Unit

interface OnItemClicked {
    val uriHandler: UriHandler
    val navigator: Navigator

    fun onRichTextFacetClicked(
        facet: FacetType? = null,
        uri: AtUri? = null,
        linkCallback: ((Uri) -> Unit)? = null,
        profileCallback: ((AtIdentifier) -> Unit)? = null,
        facetCallback: ((FacetType) -> Unit)? = null,
        postCallback: ((AtUri?) -> Unit)? = null,
    )

    fun onPostClicked(uri: AtUri)

    fun onProfileClicked(id: AtIdentifier)
}

data class ItemClicked(
    override val uriHandler: UriHandler,
    override val navigator: Navigator,
    val linkCallback: (Uri) -> Unit =  { link -> openBrowser(link.uri, uriHandler) },
    val profileCallback: (AtIdentifier) -> Unit = { onProfileClickedImmediate(it, navigator) },
    val facetCallback: (FacetType) -> Unit = {
        when(it) {
            is FacetType.UserDidMention -> {
                profileCallback(it.did)
            }
            is FacetType.UserHandleMention -> {
                profileCallback(it.handle)
            }
            is FacetType.ExternalLink -> {
                linkCallback(it.uri)
            }
            else -> {}
        }
    },
    val postCallback: (AtUri?) -> Unit = { uri ->
        if(uri != null) onPostClickedImmediate(uri, navigator)
    },
    val callbackAlways: () -> Unit = { },
): OnItemClicked {


    override fun onRichTextFacetClicked(
        facet: FacetType?,
        uri: AtUri?,
        linkCallback: ((Uri) -> Unit)?,
        profileCallback: ((AtIdentifier) -> Unit)?,
        facetCallback: ((FacetType) -> Unit)?,
        postCallback: ((AtUri?) -> Unit)?
    ) {
        val facetFun = facetCallback ?: this.facetCallback
        when(facet) {
            is FacetType.UserDidMention -> {
                if(profileCallback != null) profileCallback(facet.did)
                else this.profileCallback(facet.did)
            }
            is FacetType.UserHandleMention -> {
                if(profileCallback != null) profileCallback(facet.handle)
                else this.profileCallback(facet.handle)
            }
            is FacetType.ExternalLink -> {
                linkCallback(facet.uri)
            }
            is FacetType.Tag -> facetFun(facet)
            is FacetType.PollBlueOption -> facetFun(facet)
            is FacetType.BlueMoji -> facetFun(facet)
            is FacetType.Format -> facetFun(facet)
            FacetType.PollBlueQuestion -> facetFun(facet)
            is FacetType.UnknownFacet -> facetFun(facet)
            null -> if(postCallback != null) postCallback(uri) else this.postCallback(uri)
        }
        callbackAlways()
    }

    override fun onPostClicked(uri: AtUri) {
        postCallback(uri)
        callbackAlways()
    }

    override fun onProfileClicked(id: AtIdentifier) {
        profileCallback(id)
        callbackAlways()
    }
}

