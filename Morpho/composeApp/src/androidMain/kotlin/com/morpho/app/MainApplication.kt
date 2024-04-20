package com.morpho.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import com.morpho.app.di.appModule
import com.morpho.app.di.dataModule
import com.morpho.app.di.storageModule
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.Butterfly
import com.morpho.butterfly.auth.SessionRepository
import com.morpho.butterfly.auth.UserRepository
import kotlinx.coroutines.runBlocking
import org.koin.android.annotation.KoinViewModel
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.parameter.parametersOf


@KoinViewModel
class AndroidMainViewModel(app: Application): AndroidViewModel(app), DefaultLifecycleObserver {
    val sessionRepository = app.getKoin().get<SessionRepository> { parametersOf(app.cacheDir.path) }
    val userRepository = app.getKoin().get<UserRepository> { parametersOf(app.cacheDir.path) }
    var id: AtIdentifier? = if(sessionRepository.auth?.did != null) {
        sessionRepository.auth?.did
    } else if (sessionRepository.auth?.handle != null) {
        sessionRepository.auth?.handle
    } else {
        userRepository.firstUser()?.id
    }
    var user = if(id != null) {
        runBlocking { userRepository.findUser(id!!) }
    } else {
        null
    }
    val api = app.getKoin().get<Butterfly> { parametersOf(id) }
}

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MainApplication)
            androidLogger()
            modules(appModule, storageModule, dataModule)
        }
    }
}

