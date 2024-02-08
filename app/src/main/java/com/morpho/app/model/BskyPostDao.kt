package morpho.app.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import morpho.app.api.AtUri
import morpho.app.util.deserialize
import morpho.app.util.mapImmutable
import morpho.app.util.serialize

@Dao
interface BskyPostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPostList(post: List<CachePost>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(vararg posts: CachePost)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CachePost) : Long

    suspend fun insertThread(thread: BskyPostThread) = coroutineScope {
        insertPost(CachePost(
            uri = thread.post.uri.atUri, cid = thread.post.cid.cid,
            authorDid = thread.post.author.did.did,
            timestamp = thread.post.createdAt.instant.toEpochMilliseconds(),
            type = PostType.BskyPost,
            cacheEntry = BskyPost.serializer().serialize(thread.post).toString()
        ))
        val parentIds = mutableListOf<Long>()
        val replyIds = mutableListOf<Long>()
        val p = async {
            thread.parents.map {
                when(it) {
                    is ThreadPost.BlockedPost -> it.uri?.atUri?.let { it1 ->
                        CachePost(
                            uri = it1, cid = "",
                            authorDid = "", type = PostType.BlockedThread,
                            timestamp = thread.post.createdAt.instant.toEpochMilliseconds(),
                            cacheEntry = ThreadPost.serializer().serialize(it).toString()
                        )
                    }?.let { it2 ->
                        parentIds.add(insertPost(
                            it2
                        ))
                    }
                    is ThreadPost.NotFoundPost -> it.uri?.atUri?.let { it1 ->
                        CachePost(
                            uri = it1, cid = "",
                            authorDid = "", type = PostType.NotFoundThread,
                            timestamp = thread.post.createdAt.instant.toEpochMilliseconds(),
                            cacheEntry = ThreadPost.serializer().serialize(it).toString()
                        )
                    }?.let { it2 ->
                        parentIds.add(insertPost(
                            it2
                        ))
                    }
                    is ThreadPost.ViewablePost -> parentIds.add(insertPost(
                        CachePost(
                            uri = it.post.uri.atUri, cid = it.post.cid.cid,
                            authorDid = it.post.author.did.did, type = PostType.NotFoundThread,
                            timestamp = it.post.createdAt.instant.toEpochMilliseconds(),
                            cacheEntry = ThreadPost.serializer().serialize(it).toString()
                        )
                    ))
                }
            }
        }
        val r = async {
            thread.replies.map {
                when (it) {
                    is ThreadPost.BlockedPost -> it.uri?.atUri?.let { it1 ->
                        CachePost(
                            uri = it1, cid = "",
                            authorDid = "", type = PostType.BlockedThread,
                            timestamp = thread.post.createdAt.instant.toEpochMilliseconds(),
                            cacheEntry = ThreadPost.serializer().serialize(it).toString()
                        )
                    }?.let { it2 ->
                        replyIds.add(insertPost(
                            it2
                        ))
                    }

                    is ThreadPost.NotFoundPost -> it.uri?.atUri?.let { it1 ->
                        CachePost(
                            uri = it1, cid = "",
                            authorDid = "", type = PostType.NotFoundThread,
                            timestamp = thread.post.createdAt.instant.toEpochMilliseconds(),
                            cacheEntry = ThreadPost.serializer().serialize(it).toString()
                        )
                    }?.let { it2 ->
                        replyIds.add(insertPost(
                            it2
                        ))
                    }

                    is ThreadPost.ViewablePost -> replyIds.add(insertPost(
                        CachePost(
                            uri = it.post.uri.atUri, cid = it.post.cid.cid,
                            authorDid = it.post.author.did.did, type = PostType.NotFoundThread,
                            timestamp = it.post.createdAt.instant.toEpochMilliseconds(),
                            cacheEntry = ThreadPost.serializer().serialize(it).toString()
                        )
                    ))
                }
            }
        }
        awaitAll(p,r)
        insertThread(BskyDbThread(
            startUri = thread.post.uri.atUri, parentIds = parentIds, replyIds = replyIds
        ))
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThread(thread: BskyDbThread) : Long

    @Query("SELECT * FROM post_cache WHERE uri = :uri LIMIT 1")
    fun getPostInternal(uri: String): Flow<CachePost>

    suspend fun getPost(uri: String) = getPostInternal(uri).distinctUntilChanged()

    @Query("SELECT * FROM post_cache WHERE uri IN (:uris)")
    fun getPostListInternal(uris: List<String>): Flow<List<CachePost>>

    suspend fun getPostList(uris: List<String>) = getPostListInternal(uris).distinctUntilChanged()


    @Query("SELECT * FROM threads WHERE startUri = :startUri")
    fun getThreadStart(startUri: String): Flow<BskyDbThread>

    @Query("SELECT * FROM post_cache WHERE rowid = :rowid")
    suspend fun getPostById(rowid:Long) : CachePost


    suspend fun getThread(startUri: String): Flow<BskyPostThread> {
        val start = getThreadStart(startUri).distinctUntilChanged().first()
        val post = getPost(start.startUri).first()

        val parents: Flow<List<ThreadPost>> = flow {
            start.parentIds.map {
                val p = getPostById(it)
                when (p.type){
                    PostType.BlockedThread -> {
                        ThreadPost.BlockedPost(AtUri(p.uri))
                    }
                    PostType.NotFoundThread -> {
                        ThreadPost.NotFoundPost(AtUri(p.uri))
                    }
                    PostType.VisibleThread -> {
                        ThreadPost.ViewablePost.serializer().deserialize(p.cacheEntry)
                    }
                    else -> {}
                }
            }
        }

        val replies: Flow<ImmutableList<ThreadPost>> = flow {
            start.replyIds.mapImmutable {
                val r = getPostById(it)
                when (r.type){
                    PostType.BlockedThread -> {
                        ThreadPost.BlockedPost(AtUri(r.uri))
                    }
                    PostType.NotFoundThread -> {
                        ThreadPost.NotFoundPost(AtUri(r.uri))
                    }
                    PostType.VisibleThread -> {
                        ThreadPost.ViewablePost.serializer().deserialize(r.cacheEntry)
                    }
                    else -> {}
                }
            }
        }

        return flowOf(
            BskyPostThread(
                post = BskyPost.serializer().deserialize(post.cacheEntry),
                _parents = parents.first(),
                replies = replies.first()
            )
        ).distinctUntilChanged()
    }

    @Query("SELECT * FROM post_cache")
    fun getAll(): Flow<List<CachePost>>

}

class Converters {
    @TypeConverter
    fun StringToLongList(string: String?) : List<Long>? {
        return string?.split('|')?.map {
            it.toLong()
        }
    }

    @TypeConverter
    fun LongListToString(list: List<Long>?) : String? {
        return list?.joinToString("|")
    }
}
