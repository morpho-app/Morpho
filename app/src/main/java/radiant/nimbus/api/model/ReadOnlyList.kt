package radiant.nimbus.api.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
import radiant.nimbus.api.runtime.ImmutableListSerializer

typealias ReadOnlyList<T> = @Serializable(with = ImmutableListSerializer::class) ImmutableList<T>
