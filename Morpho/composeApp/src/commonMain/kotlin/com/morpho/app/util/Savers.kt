package com.morpho.app.util

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import com.morpho.butterfly.AtUri
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// commonMain - module core
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect interface JavaSerializable

val atUriSaver: Saver<AtUri, *> = listSaver(
    save = { listOf(it.atUri)},
    restore = {
        AtUri(it.first())
    }
)



class StateFlowSerializer<T>(private val dataSerializer: KSerializer<T>) : KSerializer<StateFlow<T>> {
    override val descriptor: SerialDescriptor = dataSerializer.descriptor
    override fun serialize(encoder: Encoder, value: StateFlow<T>) = dataSerializer.serialize(encoder, value.value)
    override fun deserialize(decoder: Decoder) = MutableStateFlow(dataSerializer.deserialize(decoder)).asStateFlow()
}

class MutableStateFlowSerializer<T>(private val dataSerializer: KSerializer<T>) : KSerializer<MutableStateFlow<T>> {
    override val descriptor: SerialDescriptor = dataSerializer.descriptor
    override fun serialize(encoder: Encoder, value: MutableStateFlow<T>) = dataSerializer.serialize(encoder, value.value)
    override fun deserialize(decoder: Decoder) = MutableStateFlow(dataSerializer.deserialize(decoder))
}

class MutableSharedFlowSerializer<T>(private val dataSerializer: KSerializer<T>) : KSerializer<MutableSharedFlow<T>> {
    override val descriptor: SerialDescriptor = dataSerializer.descriptor
    override fun serialize(encoder: Encoder, value: MutableSharedFlow<T>) = dataSerializer.serialize(encoder, value.replayCache.last())
    override fun deserialize(decoder: Decoder): MutableSharedFlow<T> {
        val flow = MutableSharedFlow<T>(
            replay = 1,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        flow.tryEmit(dataSerializer.deserialize(decoder))
        return flow
    }
}