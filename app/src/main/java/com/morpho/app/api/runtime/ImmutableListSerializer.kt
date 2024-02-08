package morpho.app.api.runtime

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.listSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = ImmutableList::class)
class ImmutableListSerializer<T>(
  dataSerializer: KSerializer<T>,
) : KSerializer<ImmutableList<T>> {
  private val listSerializer = ListSerializer(dataSerializer)

  override val descriptor: SerialDescriptor = ImmutableListDescriptor<T>(dataSerializer.descriptor)

  override fun serialize(
    encoder: Encoder,
    value: ImmutableList<T>,
  ) {
    return listSerializer.serialize(encoder, value.toList())
  }

  override fun deserialize(decoder: Decoder): ImmutableList<T> {
    return listSerializer.deserialize(decoder).toImmutableList()
  }

  private class ImmutableListDescriptor<T>(
    private val elementDescriptor: SerialDescriptor,
  ) : SerialDescriptor by listSerialDescriptor(elementDescriptor) {
    override val serialName: String = ImmutableList::class.qualifiedName!!
  }
}
