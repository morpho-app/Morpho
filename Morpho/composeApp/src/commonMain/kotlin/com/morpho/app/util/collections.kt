package com.morpho.app.util

import com.morpho.butterfly.model.ReadOnlyList
import kotlinx.collections.immutable.toImmutableList

inline fun <T, R> Iterable<T>.mapImmutable(transform: (T) -> R): ReadOnlyList<R> {
    return map { transform(it) }.toImmutableList()
}

inline fun <T, R> Iterable<T>.flatMapImmutable(transform: (T) -> Iterable<R>): ReadOnlyList<R> {
    return flatMap { transform(it) }.toImmutableList()
}

fun <T> ReadOnlyList<T>.plus(iterable: Iterable<T>): ReadOnlyList<T> {
    return (this + iterable).toImmutableList()
}




