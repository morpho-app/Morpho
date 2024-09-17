package com.morpho.app

import android.app.Application
import com.gu.toolargetool.TooLargeTool
import com.morpho.app.data.PreferencesRepository
import com.morpho.app.di.appModule
import com.morpho.app.di.dataModule
import com.morpho.app.di.storageModule
import com.morpho.butterfly.auth.SessionRepository
import com.morpho.butterfly.auth.UserRepository
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.parameter.parametersOf


class MorphoApplication : Application() {
    override fun onCreate() {
        TooLargeTool.startLogging(this);

        val koin = startKoin {
            androidContext(this@MorphoApplication)
            androidLogger()
            modules(appModule, storageModule, dataModule)
        }.koin
        val sessionRepository = koin.get<SessionRepository> { parametersOf(cacheDir.path.toString()) }
        val userRepository = koin.get<UserRepository> { parametersOf(cacheDir.path.toString()) }
        val prefs = koin.get<PreferencesRepository> { parametersOf(cacheDir.path.toString()) }
        super.onCreate()
    }
}


