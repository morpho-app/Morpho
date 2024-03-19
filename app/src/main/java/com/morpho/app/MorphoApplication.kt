package com.morpho.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.morpho.butterfly.Butterfly
import com.morpho.butterfly.auth.LoginRepository
import com.morpho.butterfly.auth.RelayRepository
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

val Context.butterfly: Butterfly
    get() = Butterfly(RelayRepository(applicationContext.cacheDir.path.toString(), "default"), user)

var Context.user: LoginRepository
    get() = LoginRepository(applicationContext.cacheDir.path.toString(), "default")
    set(value) {
        value.auth?.let { auth-> value.credentials?.let { credentials ->
            LoginRepository(value.dir, value.key, auth,
                credentials
            )
        } }
    }

@HiltAndroidApp
class MorphoApplication : Application(), Configuration.Provider, ImageLoaderFactory {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        val notificationChannel= NotificationChannel(
            "morpho_notification",
            "Morpho",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager=getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(applicationContext).memoryCache {
            MemoryCache.Builder(applicationContext)
                .maxSizePercent(0.25)
                .build()
            }.diskCache {
                DiskCache.Builder()
                    .directory(applicationContext.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }.dispatcher(Dispatchers.Default.limitedParallelism(8))
            .build()
    }

}