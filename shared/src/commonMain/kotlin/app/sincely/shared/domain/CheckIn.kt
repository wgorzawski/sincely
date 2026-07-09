package app.sincely.shared.domain

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** A single recorded "I did the thing" event for a tracker. */
@Serializable
data class CheckIn(
    val id: Long,
    val trackerId: Long,
    @Serializable(with = InstantSerializer::class)
    val timestamp: Instant,
    val note: String? = null,
    /** True when logged for a date earlier than "now" (e.g. "wczoraj" / custom date). */
    val backdated: Boolean = false,
)
