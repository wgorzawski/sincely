package app.sincely.android.di

import app.sincely.android.ui.TrackerListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val androidAppModule = module {
    viewModel { TrackerListViewModel(get()) }
}
