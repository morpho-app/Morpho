package radiant.nimbus.util

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

inline fun <T, R> Iterable<T>.mapImmutable(transform: (T) -> R): ImmutableList<R> {
    return map { transform(it) }.toImmutableList()
}

inline fun <T, R> Iterable<T>.flatMapImmutable(transform: (T) -> Iterable<R>): ImmutableList<R> {
    return flatMap { transform(it) }.toImmutableList()
}

fun <T> ImmutableList<T>.plus(iterable: Iterable<T>): ImmutableList<T> {
    return (this + iterable).toImmutableList()
}




/**
 * Workaround for https://github.com/Kotlin/kotlinx.serialization/issues/2288
 *
 * When serializing a sealed value class, the serial name of the inner class is embedded into the
 * resulting JSON string. However, the produced string cannot be deserialized, as it is the outer
 * class's serial name (ie: the sealed value class itself) that should be used here.
 */
inline fun <reified T : Any> valueClassSerializer(): KSerializer<T> {
    return SealedValueClassSerialNameKSerializer(T::class)
}

@OptIn(ExperimentalSerializationApi::class)
class SealedValueClassSerialNameKSerializer<T : Any>(
    kClass: KClass<T>,
) : KSerializer<T> {
    private val constructor = kClass.primaryConstructor!!
    private val valueField = kClass.members.single { it.name == "value" }
    private val serialName = kClass.annotations.filterIsInstance<SerialName>().single().value

    private val innerSerializer = serializer(valueField.returnType)
    private val descriptorMapping = mapOf(
        innerSerializer.descriptor to SerialDescriptor(serialName, innerSerializer.descriptor),
    )

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(serialName)

    override fun serialize(
        encoder: Encoder,
        value: T,
    ) {
        val overrideEncoder = OverrideDescriptorEncoder(descriptorMapping, encoder)
        innerSerializer.serialize(overrideEncoder, valueField.call(value))
    }

    override fun deserialize(decoder: Decoder): T {
        val overrideDecoder = OverrideDescriptorDecoder(descriptorMapping, decoder)
        return constructor.call(innerSerializer.deserialize(overrideDecoder.decodeInline(descriptor)))
    }
}

class OverrideDescriptorEncoder(
    private val descriptorMapping: Map<SerialDescriptor, SerialDescriptor>,
    private val base: Encoder,
) : Encoder by base {
    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        return base.beginStructure(descriptorMapping[descriptor] ?: descriptor)
    }
}

class OverrideDescriptorDecoder(
    private val descriptorMapping: Map<SerialDescriptor, SerialDescriptor>,
    private val base: Decoder,
) : Decoder by base {
    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return base.beginStructure(descriptorMapping[descriptor] ?: descriptor)
    }
}