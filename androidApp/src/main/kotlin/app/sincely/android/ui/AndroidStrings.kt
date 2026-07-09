package app.sincely.android.ui

/** Hardcoded Polish UI copy, kept out of Composables so it's easy to swap for real i18n later. */
object AndroidStrings {
    const val APP_TITLE = "Kiedy ostatnio?"
    const val EMPTY_LIST_TITLE = "jeszcze nic tu nie ma"
    const val EMPTY_LIST_SUBTITLE = "dodaj pierwszą rzecz, którą chcesz śledzić — spokojnie, bez pośpiechu"
    const val POPULAR_SECTION = "popularne"
    const val FILTER_ALL = "Wszystkie"
    const val FILTERED_EMPTY = "brak trackerów w tej kategorii"
    const val ADD_TRACKER_CONTENT_DESCRIPTION = "Dodaj tracker"
    const val TARGET_DAYS_FORMAT = "co %d dni"
    const val NO_TARGET = "bez celu"
    const val CHECKED_IN_TOAST = "Odhaczono ✓"
    const val UNDO = "Cofnij"
    const val CANCEL = "Anuluj"

    // add / edit sheet
    const val NEW_TRACKER_TITLE = "nowy tracker"
    const val NAME_LABEL = "nazwa"
    const val NAME_PLACEHOLDER = "np. Podlewanie kwiatów"
    const val EMOJI_LABEL = "emoji"
    const val CATEGORY_LABEL = "kategoria"
    const val CUSTOM_CATEGORY_PLACEHOLDER = "nazwa własnej kategorii"
    const val INTERVAL_TOGGLE_LABEL_ADD = "przypominaj o terminie"
    const val INTERVAL_TOGGLE_LABEL_EDIT = "interwał docelowy"
    const val INTERVAL_DAYS_LABEL = "co ile dni"
    const val REMINDER_TOGGLE_LABEL = "przypomnij, gdy minie termin"
    const val REMINDER_TIME_LABEL = "pora przypomnienia"
    const val REMINDER_MORNING = "rano 9:00"
    const val REMINDER_EVENING = "wieczorem 19:00"
    const val NOTIF_PREVIEW_LABEL = "podgląd powiadomienia"
    const val NOTIF_SUBTITLE = "Kiedy ostatnio? · teraz"
    const val SUBMIT_ADD = "dodaj"

    fun notifBody(days: Int) = "minęło $days dni (cel: co $days)"

    // detail screen
    const val DETAIL_TITLE = "szczegóły"
    const val STATS_SECTION = "statystyki"
    const val NO_STATS_YET = "za mało danych — zrób jeszcze jeden check-in"
    const val EDIT_SECTION = "edytuj"
    const val HISTORY_SECTION = "historia"
    const val UNDO_LAST_CHECKIN = "cofnij ostatni check-in"
    const val ARCHIVE = "archiwizuj"
    const val DELETE = "usuń"
    const val LATEST_BADGE = "ostatni"
    const val GOAL_BADGE_FORMAT = "cel: co %d dni"

    fun bigLabel(days: Long): String = when (days) {
        0L -> "dziś"
        1L -> "wczoraj"
        else -> "dni temu"
    }

    fun comparisonText(averageDays: Long, targetDays: Int?): String =
        if (targetDays != null) "średnio co $averageDays dni · cel co $targetDays"
        else "średnio co $averageDays dni"

    fun longestGapText(longestDays: Long): String = "najdłuższa przerwa: $longestDays dni"

    // check-in with options sheet
    const val CHECKIN_OPTIONS_TITLE = "check-in z opcjami"
    const val CHECKIN_DATE_LABEL = "data"
    const val CHECKIN_NOTE_LABEL = "notatka (opcjonalnie)"
    const val CHECKIN_NOTE_PLACEHOLDER = "np. 142 500 km"
    const val CHECKIN_NOW = "Teraz"
    const val CHECKIN_YESTERDAY = "Wczoraj"
    const val CHECKIN_DAY_BEFORE = "Przedwczoraj"
    const val CHECKIN_SUBMIT = "zapisz check-in"

    const val CUSTOM_CATEGORY_CHIP = "Własna"
}
