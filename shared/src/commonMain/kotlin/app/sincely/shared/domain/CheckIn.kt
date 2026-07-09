package app.sincely.shared.domain

import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** A single recorded "I did the thing" event for a tracker. */
@Serializable
data class CheckIn(
    val id: Long,
    val trackerId: Long,
    @Serializable(with = InstantIso8601Serializer::class)
    val timestamp: Instant,
    val note: String? = null,
)
