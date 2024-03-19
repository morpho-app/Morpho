package com.morpho.app.model

import app.bsky.feed.PostEmbedUnion
import app.bsky.feed.PostLabelsUnion
import app.bsky.richtext.Facet
import com.morpho.app.model.BskyPost
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Language

@Serializable
data class DraftPost(
    val text: String = "",
    val facets: MutableList<Facet> = mutableListOf(),
    val reply: BskyPost? = null,
    val embed: PostEmbedUnion? = null,
    val quote: BskyPost? = null,
    val langs: MutableList<Language> = mutableListOf(),
    val labels: PostLabelsUnion? = null,
    /**
     * Additional non-inline tags describing this post.
     */
    val tags: MutableList<String> = mutableListOf(),

    )