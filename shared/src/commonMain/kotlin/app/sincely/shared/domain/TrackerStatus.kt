package app.sincely.shared.domain

/**
 * Health of a tracker relative to its target cadence, derived purely from
 * timestamps — never stored, always recomputed from the last check-in.
 */
enum class TrackerStatus {
    OK,
    WARNING,
    OVERDUE,
}
