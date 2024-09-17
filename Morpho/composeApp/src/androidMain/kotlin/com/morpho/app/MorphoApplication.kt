package com.morpho.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import com.gu.toolargetool.TooLargeTool
import com.morpho.app.data.MorphoAgent
import com.morpho.app.data.PreferencesRepository
import com.morpho.app.di.appModule
import com.morpho.app.di.dataModule
import com.morpho.app.di.storageModule
import com.morpho.butterfly.auth.SessionRepository
import com.morpho.butterfly.auth.UserRepository
import org.koin.android.annotation.KoinViewModel
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module


@KoinViewModel
class AndroidMainViewModel(app: Application): AndroidViewModel(app), DefaultLifecycleObserver {

    val agent = app.getKoin().get<MorphoAgent>()
}

class MorphoApplication : Application() {
    override fun onCreate() {
        TooLargeTool.startLogging(this);

        val koin = startKoin {
            androidContext(this@MorphoApplication)
            androidLogger()
            modules(androidModule, appModule, storageModule, dataModule)
        }.koin
        val sessionRepository = koin.get<SessionRepository> { parametersOf(cacheDir.path.toString()) }
        val userRepository = koin.get<UserRepository> { parametersOf(cacheDir.path.toString()) }
        val prefs = koin.get<PreferencesRepository> { parametersOf(cacheDir.path.toString()) }
        val agent = koin.get<MorphoAgent>()
        super.onCreate()
    }
}


val androidModule = module {
    viewModel<AndroidMainViewModel> { AndroidMainViewModel(androidApplication()) }
}