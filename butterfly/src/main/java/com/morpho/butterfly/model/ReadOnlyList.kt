package com.morpho.butterfly.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
import com.morpho.butterfly.ImmutableListSerializer

typealias ReadOnlyList<T> = @Serializable(with = ImmutableListSerializer::class) ImmutableList<T>
