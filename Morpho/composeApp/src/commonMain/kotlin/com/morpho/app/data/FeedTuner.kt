package com.morpho.app.data

import com.morpho.app.model.bluesky.*
import com.morpho.app.model.uidata.MorphoData
import com.morpho.app.model.uidata.areSameAuthor
import com.morpho.app.model.uistate.FeedType
import com.morpho.butterfly.*
import com.morpho.butterfly.BskyPreferences
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

typealias TunerFunction<Data> = (List<Data>, FeedTuner<Data>) -> List<Data>

@Serializable
data class FeedTuner<Data: MorphoDataItem.FeedItem>(val tuners: List<TunerFunction<Data>> = persistentListOf()) {
    val seenKeys = mutableSetOf<String>()
    val seenUris = mutableSetOf<AtUri>()
    val seenRootUris = mutableSetOf<AtUri>()

    companion object {
        fun <Data: MorphoDataItem.FeedItem> useFeedTuners(
            userDid: Did,
            prefs: BskyPreferences,
            desc: FeedDescriptor,
        ): List<FeedTuner<Data>> {
            if(desc is FeedDescriptor.Author) {
                when(desc.filter) {
                    AuthorFilter.PostsNoReplies -> return listOf(
                        FeedTuner(tuners = persistentListOf(
                            Companion::removeReplies
                        )) as FeedTuner<Data>
                    )
                    AuthorFilter.PostsWithReplies ->{
                        return listOf()
                    }
                    AuthorFilter.PostsAuthorThreads -> {
                        return listOf()
                    }
                    AuthorFilter.PostsWithMedia -> {
                        return listOf()
                    }
                }
            }
            val languages = prefs.languages
            val languageTuner: TunerFunction<MorphoDataItem.FeedItem> = { f, t ->
                preferredLanguageOnly(languages, f, t)
            }
            if(desc is FeedDescriptor.FeedGen) {
                return listOf(FeedTuner(tuners = persistentListOf(languageTuner))) as List<FeedTuner<Data>>
            }
            if(desc is FeedDescriptor.Home || desc is FeedDescriptor.List) {
                val tuners = mutableListOf(FeedTuner(tuners = persistentListOf(Companion::removeOrphans)))
                val feedPrefs = prefs.feedView ?: return tuners.toList() as List<FeedTuner<Data>>
                if(feedPrefs.hideReposts == true) tuners.add(FeedTuner(tuners = persistentListOf(Companion::removeReposts)))
                if(feedPrefs.hideReplies == true) tuners.add(FeedTuner(tuners = persistentListOf(Companion::removeReplies)))
                else {
                    val followedRepliesOnly: TunerFunction<MorphoDataItem.FeedItem> = { f, t ->
                        followedRepliesOnly(userDid, f, t)
                    }
                    tuners.add(FeedTuner(tuners = persistentListOf(followedRepliesOnly)))
                }
                if(feedPrefs.hideQuotePosts == true) tuners.add(
                    FeedTuner(tuners = persistentListOf(
                        Companion::removeQuotePosts
                    ))
                )
                tuners.add(FeedTuner(tuners = persistentListOf(Companion::dedupThreads)))
                return tuners.toList() as List<FeedTuner<Data>>
            }
            return listOf()
        }

        fun <Data: MorphoDataItem.FeedItem> useFeedTuners(
            prefs: BskyUserPreferences,
            feed: MorphoData<Data>
        ): List<FeedTuner<Data>> {
            if(feed.isProfileFeed) {
                when(feed.feedType) {
                    FeedType.PROFILE_POSTS -> return listOf(
                        FeedTuner(tuners = persistentListOf(
                            Companion::removeReplies
                        )) as FeedTuner<Data>
                    )
                    FeedType.PROFILE_USER_LISTS -> return listOf()
                    FeedType.PROFILE_FEEDS_LIST -> return listOf()
                    FeedType.PROFILE_MOD_SERVICE -> return listOf()
                    else -> {}
                }
            }
            val languages = prefs.preferences.languages.toList()
            val languageTuner: TunerFunction<MorphoDataItem.FeedItem> = { f, t ->
                preferredLanguageOnly(languages, f, t)
            }
            if(feed.feedType == FeedType.OTHER) {
                return listOf(FeedTuner(tuners = persistentListOf(languageTuner))) as List<FeedTuner<Data>>
            }
            if(feed.feedType == FeedType.LIST_FOLLOWING || feed.feedType == FeedType.HOME) {
                val userDid = Did(prefs.user.userDid)
                val tuners = mutableListOf(FeedTuner(tuners = persistentListOf(Companion::removeOrphans)))
                val feedPrefs = prefs.preferences.feedViewPrefs[feed.uri.atUri] ?:
                    return tuners.toList() as List<FeedTuner<Data>>
                if(feedPrefs.hideReposts) tuners.add(FeedTuner(tuners = persistentListOf(Companion::removeReposts)))
                if(feedPrefs.hideReplies) tuners.add(FeedTuner(tuners = persistentListOf(Companion::removeReplies)))
                else {
                    val followedRepliesOnly: TunerFunction<MorphoDataItem.FeedItem> = { f, t ->
                        followedRepliesOnly(userDid, f, t)
                    }
                    tuners.add(FeedTuner(tuners = persistentListOf(followedRepliesOnly)))
                }
                if(feedPrefs.hideQuotePosts) tuners.add(
                    FeedTuner(tuners = persistentListOf(
                        Companion::removeQuotePosts
                    ))
                )
                tuners.add(FeedTuner(tuners = persistentListOf(Companion::dedupThreads)))
                return tuners.toList() as List<FeedTuner<Data>>
            }
            return listOf()
        }

        fun <Data: MorphoDataItem.FeedItem> removeReplies(
            feed: List<Data>,
            tuner: FeedTuner<Data> = FeedTuner(),
        ): List<Data> {
            return feed.filterNot { item ->
                when(item) {
                    is MorphoDataItem.Post -> item.isReply && !item.isRepost &&
                            !(item.getAuthors()?.let { areSameAuthor(it) } ?: false)
                    is MorphoDataItem.Thread -> !(item.getAuthors()?.let { areSameAuthor(it) } ?: false)
                    else -> false
                }
            }

        }

        fun  <Data: MorphoDataItem.FeedItem> removeReposts(
            feed: List<Data>,
            tuner: FeedTuner<Data> = FeedTuner(),
        ): List<Data> {
            return feed.filterNot { item ->
                item.isRepost
            }
        }

        fun <Data: MorphoDataItem.FeedItem> removeQuotePosts(
            feed: List<Data>,
            tuner: FeedTuner<Data> = FeedTuner(),
        ): List<Data> {
            return feed.filterNot { item ->
                item.isQuotePost
            }
        }

        fun <Data: MorphoDataItem.FeedItem> removeOrphans(
            feed: List<Data>,
            tuner: FeedTuner<Data> = FeedTuner(),
        ): List<Data> {
            return feed.filterNot { item ->
                when(item) {
                    is MorphoDataItem.Post -> item.isOrphan
                    is MorphoDataItem.Thread -> false
                    else -> false
                }
            }
        }

        fun <Data: MorphoDataItem.FeedItem> dedupThreads(
            feed: List<Data>,
            tuner: FeedTuner<Data> = FeedTuner(),
        ): List<Data> {
            return feed.filterNot { item ->
                val rootUri = item.rootUri
                if(!item.isRepost == tuner.seenRootUris.contains(rootUri)) {
                    false
                } else {
                    tuner.seenRootUris.add(rootUri)
                    true
                }
            }
        }

        fun <Data: MorphoDataItem.FeedItem> followedRepliesOnly(
            userDid: Did,
            feed: List<Data>,
            tuner: FeedTuner<Data> = FeedTuner(),
        ): List<Data> {
            return feed.filterNot { item ->
                item.isReply && !shouldDisplayReplyInFollowing(item, userDid)
            }
        }

        fun <Data: MorphoDataItem.FeedItem> preferredLanguageOnly(
            languages: List<Language> = persistentListOf(),
            feed: List<Data>,
            tuner: FeedTuner<Data> = FeedTuner(),
        ): List<Data> {
            if (languages.isEmpty()) return feed
            val newFeed = feed.filter { item ->
                when(item) {
                    is MorphoDataItem.Post -> {
                        item.post.langs.isEmpty() ||
                                item.post.langs.any { languages.contains(it) }
                    }
                    is MorphoDataItem.Thread -> {
                        item.thread.post.langs.isEmpty() ||
                                item.thread.post.langs.any { languages.contains(it) }
                    }
                    else -> false
                }
            }.map { item ->
                when(item) {
                    is MorphoDataItem.Post -> item
                    is MorphoDataItem.Thread -> {
                        item.copy(
                            thread = item.thread.filterReplies { reply ->
                                when(reply) {
                                    is ThreadPost.ViewablePost -> reply.post.langs.isEmpty() ||
                                            reply.post.langs.any { languages.contains(it) }
                                    is ThreadPost.BlockedPost -> true
                                    is ThreadPost.NotFoundPost -> true
                                }

                            }
                        )
                    }
                    else -> false
                }
            }
            return newFeed.ifEmpty { feed } as List<Data>
        }
    }
    fun tune(
        feed: MorphoData<Data>
    ): MorphoData<Data> {
        var workingFeed = feed.items
        tuners.forEach { tuner ->
            workingFeed = tuner(workingFeed, this)
        }
        workingFeed = workingFeed.map { item ->
            if(seenKeys.contains(item.key)) return@map null
            else if(item is MorphoDataItem.Thread) {
                val itemUris = item.getUris()
                val seenInThisThread = itemUris.filter { seenUris.contains(it) }
                if(seenInThisThread.isNotEmpty()) {
                    if(seenInThisThread.size == itemUris.size) {
                        return@map null
                    } else {
                        val newParents = item.thread.parents.filter { parent ->
                            when(parent) {
                                is ThreadPost.ViewablePost -> parent.post.uri in seenInThisThread
                                is ThreadPost.BlockedPost -> false
                                is ThreadPost.NotFoundPost -> false
                            }
                        }
                        val newThread = item.copy(thread = item.thread.filterReplies { reply ->
                            when(reply) {
                                is ThreadPost.ViewablePost -> reply.post.uri in seenInThisThread
                                is ThreadPost.BlockedPost -> false
                                is ThreadPost.NotFoundPost -> false
                            }
                        }.copy(parents = newParents))
                        seenUris.addAll(itemUris)
                        if(newThread.thread.replies.isEmpty() && newThread.thread.parents.isEmpty()) {
                            return@map null
                        } else {
                            return@map newThread
                        }
                    }
                } else {
                    seenUris.addAll(itemUris)
                    item
                }
            } else {
                val disableDedub = item.isReply && item.isRepost
                if(!disableDedub) seenKeys.add(item.key)
                item
            }
        }.filterNotNull() as List<Data>
        return feed.copy(items = workingFeed)
    }
    fun tune(
        feed: PagedResponse.Feed<Data>
    ): PagedResponse.Feed<Data> {
        var workingFeed = feed.items
        tuners.forEach { tuner ->
            workingFeed = tuner(workingFeed, this)
        }
        workingFeed = workingFeed.map { item ->
            if(seenKeys.contains(item.key)) return@map null
            else if(item is MorphoDataItem.Thread) {
                val itemUris = item.getUris()
                val seenInThisThread = itemUris.filter { seenUris.contains(it) }
                if(seenInThisThread.isNotEmpty()) {
                    if(seenInThisThread.size == itemUris.size) {
                        return@map null
                    } else {
                        val newParents = item.thread.parents.filter { parent ->
                            when(parent) {
                                is ThreadPost.ViewablePost -> parent.post.uri in seenInThisThread
                                is ThreadPost.BlockedPost -> false
                                is ThreadPost.NotFoundPost -> false
                            }
                        }
                        val newThread = item.copy(thread = item.thread.filterReplies { reply ->
                            when(reply) {
                                is ThreadPost.ViewablePost -> reply.post.uri in seenInThisThread
                                is ThreadPost.BlockedPost -> false
                                is ThreadPost.NotFoundPost -> false
                            }
                        }.copy(parents = newParents))
                        seenUris.addAll(itemUris)
                        if(newThread.thread.replies.isEmpty() && newThread.thread.parents.isEmpty()) {
                            return@map null
                        } else {
                            return@map newThread
                        }
                    }
                } else {
                    seenUris.addAll(itemUris)
                    item
                }
            } else {
                val disableDedub = item.isReply && item.isRepost
                if(!disableDedub) seenKeys.add(item.key)
                item
            }
        }.filterNotNull() as List<Data>
        return feed.copy(items = workingFeed)
    }


}

/// Algo copied from official app
/// https://github.com/bluesky-social/social-app/blob/main/src/lib/api/feed-manip.ts#L445
/// as of commit https://github.com/bluesky-social/social-app/commit/e2a244b99889743a8788b0c464d3e150bc8047ad
/// The algorithm is a controversial, so we may want to change it or offer more options.
fun shouldDisplayReplyInFollowing(
    item: MorphoDataItem.FeedItem,
    userDid: Did,
): Boolean {
    val authors = item.getAuthors()
    val author = authors?.author
    val rootAuthor = authors?.rootAuthor
    val parentAuthor = authors?.parentAuthor
    val grandParentAuthor = authors?.grandParentAuthor
    if (!isSelfOrFollowing(author, userDid))
        return false // Only show replies from self or people you follow.

    if(parentAuthor == null || parentAuthor.did == author?.did
        && rootAuthor == null || rootAuthor?.did == author?.did
        && grandParentAuthor == null || grandParentAuthor?.did == author?.did
    ) return true // Always show self-threads.

    if (
        parentAuthor.did != author?.did &&
        rootAuthor?.did == author?.did &&
        item is MorphoDataItem.Thread
    ) {
        // If you follow A, show A -> someone[>0 likes] -> A chains too.
        // This is different from cases below because you only know one person.
        val parentPost = when(val p = item.thread.parents.lastOrNull()) {
            is ThreadPost.ViewablePost -> p.post
            else -> null
        }
        if(parentPost != null && parentPost.likeCount > 0)
            return true
    }
    // From this point on we need at least one more reason to show it.
    if (
        parentAuthor.did != author?.did && isSelfOrFollowing(parentAuthor, userDid)
    ) return true
    if (
        grandParentAuthor != null &&
        grandParentAuthor.did != author?.did &&
        isSelfOrFollowing(grandParentAuthor, userDid)
    ) return true
    if (
        rootAuthor != null &&
        rootAuthor.did != author?.did &&
        isSelfOrFollowing(rootAuthor, userDid)
    ) return true
    return false
}

fun isSelfOrFollowing(profile: Profile?, userDid: Did): Boolean {
    return profile?.did == userDid || profile?.followedByMe == true
}