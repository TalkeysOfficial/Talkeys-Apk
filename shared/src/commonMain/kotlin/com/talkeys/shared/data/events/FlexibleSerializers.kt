package com.talkeys.shared.data.events

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull

/**
 * Serializer for fields that the backend sends as either a JSON number or a
 * JSON string. Exposes the value as a Kotlin [String] so monetary values
 * (ticketPrice) are never silently truncated by floating-point conversion.
 *
 * Accepts:
 *   - JSON string  -> kept as-is
 *   - JSON number  -> converted to its literal text representation
 * Invalid or blank values fail decoding so a broken backend contract is visible.
 */
internal object FlexibleStringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleString", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return decoder.decodeString()
        val element = jsonDecoder.decodeJsonElement() as? JsonPrimitive
            ?: throw SerializationException("Expected a primitive monetary value")
        return element.content.takeIf { it.isNotBlank() }
            ?: throw SerializationException("Expected a non-blank monetary value")
    }

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}

/**
 * Serializer for integer fields that the backend may send as a JSON string
 * (e.g. `"100"` instead of `100`). Exposes the value as a Kotlin [Int].
 *
 * Accepts:
 *   - JSON number  -> taken as int
 *   - JSON string  -> parsed to int
 *
 * Invalid/non-integral values fail decoding rather than appearing to be zero seats.
 */
internal object FlexibleIntSerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleInt", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Int {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return decoder.decodeInt()
        val element = jsonDecoder.decodeJsonElement() as? JsonPrimitive
            ?: throw SerializationException("Expected an integer primitive")
        return element.intOrNull ?: element.content.toIntOrNull()
            ?: throw SerializationException("Expected an integer value, received ${element.content}")
    }

    override fun serialize(encoder: Encoder, value: Int) {
        encoder.encodeInt(value)
    }
}
