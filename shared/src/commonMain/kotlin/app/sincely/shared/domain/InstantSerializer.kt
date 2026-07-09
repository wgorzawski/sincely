package app.sincely.shared.domain

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

/**
 * ISO-8601 string serializer for [Instant]. kotlinx-datetime's own
 * `InstantIso8601Serializer` doesn't resolve for the `kotlin.time.Instant`
 * typealias on the Kotlin/Native targets used here, so we serialize via
 * `Instant.toString()`/`Instant.parse()` directly instead.
 */
object InstantSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant =
        Instant.parse(decoder.decodeString())
}
