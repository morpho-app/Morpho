package com.morpho.app.ui.common

import android.content.Intent
import android.net.Uri
import com.morpho.app.MainApplication
import com.morpho.app.model.bluesky.BskyPost

actual fun sharePost(post: BskyPost) {

    val postUrl = post.uri.atUri
        .replaceBefore("post", "https://bsky.app/profile/${post.author.handle}/")
    val shareIntent = Intent.createChooser(Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, postUrl)
        putExtra(Intent.EXTRA_TITLE, "Post by ${post.author.displayName.orEmpty()} ${post.author.handle}")

        data = Uri.parse(post.author.avatar.orEmpty())
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }, null)

    MainApplication().applicationContext.startActivity(shareIntent)
}