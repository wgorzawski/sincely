import SwiftUI
import shared

@main
struct iosAppApp: App {

    init() {
        KoinHelper.shared.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
