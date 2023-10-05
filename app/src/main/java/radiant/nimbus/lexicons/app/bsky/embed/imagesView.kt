package app.bsky.embed

import kotlinx.serialization.Serializable
import radiant.nimbus.api.model.ReadOnlyList

@Serializable
public data class ImagesView(
  public val images: ReadOnlyList<ImagesViewImage>,
) {
  init {
    require(images.count() <= 4) {
      "images.count() must be <= 4, but was ${images.count()}"
    }
  }
}
