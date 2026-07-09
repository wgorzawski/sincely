package app.sincely.android

import android.app.Application
import app.sincely.android.di.androidAppModule
import app.sincely.shared.di.platformModule
import app.sincely.shared.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class SincelyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@SincelyApplication)
            modules(sharedModule, platformModule(), androidAppModule)
        }
    }
}
