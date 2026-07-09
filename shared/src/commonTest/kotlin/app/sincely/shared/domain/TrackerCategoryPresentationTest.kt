package app.sincely.shared.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class TrackerCategoryPresentationTest {

    @Test
    fun label_builtInCategories_ignoreCustomLabel() {
        assertEquals("Dom", TrackerCategoryPresentation.label(TrackerCategory.DOM, customLabel = "cokolwiek"))
        assertEquals("Auto", TrackerCategoryPresentation.label(TrackerCategory.AUTO))
        assertEquals("Zdrowie", TrackerCategoryPresentation.label(TrackerCategory.ZDROWIE))
        assertEquals("Serwer", TrackerCategoryPresentation.label(TrackerCategory.SERWER))
        assertEquals("Inne", TrackerCategoryPresentation.label(TrackerCategory.INNE))
    }

    @Test
    fun label_custom_usesTrimmedCustomLabel() {
        assertEquals("Rower", TrackerCategoryPresentation.label(TrackerCategory.CUSTOM, customLabel = "  Rower  "))
    }

    @Test
    fun label_custom_blankOrMissingFallsBackToWlasna() {
        assertEquals("Własna", TrackerCategoryPresentation.label(TrackerCategory.CUSTOM, customLabel = null))
        assertEquals("Własna", TrackerCategoryPresentation.label(TrackerCategory.CUSTOM, customLabel = "   "))
    }

    @Test
    fun tracker_categoryLabelAndEmoji_delegateToPresentation() {
        val tracker = Tracker(
            id = 1,
            name = "Rower",
            emoji = "🚲",
            targetDays = null,
            category = TrackerCategory.CUSTOM,
            customCategoryLabel = "Rower",
            createdAt = kotlin.time.Instant.parse("2026-07-06T12:00:00Z"),
        )

        assertEquals("Rower", tracker.categoryLabel)
        assertEquals("🔖", tracker.categoryEmoji)
    }
}
