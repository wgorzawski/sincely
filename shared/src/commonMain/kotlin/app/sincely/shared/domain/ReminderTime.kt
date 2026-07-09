package app.sincely.shared.domain

/** Time of day a reminder notification would fire, once real scheduling exists. */
enum class ReminderTime {
    RANO,
    WIECZOREM,
}

object ReminderTimePresentation {
    fun clockLabel(time: ReminderTime): String = when (time) {
        ReminderTime.RANO -> "rano 9:00"
        ReminderTime.WIECZOREM -> "wieczorem 19:00"
    }
}
