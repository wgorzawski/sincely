package app.sincely.shared.di

import org.koin.core.context.startKoin

/** Called once from `iosAppApp.swift` on launch. */
object KoinHelper {
    fun doInitKoin() {
        startKoin {
            modules(sharedModule, platformModule())
        }
    }
}
