import Foundation

/// Hardcoded Polish UI copy, kept out of views so it's easy to swap for real i18n later.
enum IosStrings {
    static let emptyList = "Brak trackerów. Dodaj pierwszy, żeby zacząć."
    static let addTrackerAccessibilityLabel = "Dodaj tracker"
    static let noTarget = "bez celu"
    static let navigationTitle = "Sincely"

    static func targetDays(_ days: Int) -> String {
        "co \(days) dni"
    }
}
