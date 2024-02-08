package morpho.app.api.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
import morpho.app.api.runtime.ImmutableListSerializer

typealias ReadOnlyList<T> = @Serializable(with = ImmutableListSerializer::class) ImmutableList<T>
