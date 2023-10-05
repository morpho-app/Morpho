package radiant.nimbus

import android.app.Application
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.room.Room
import dagger.hilt.android.lifecycle.HiltViewModel
import radiant.nimbus.api.ApiProvider
import radiant.nimbus.api.ServerRepository
import radiant.nimbus.api.auth.LoginRepository
import radiant.nimbus.app.Supervisor
import radiant.nimbus.base.BaseViewModel
import radiant.nimbus.model.AppDatabase
import radiant.nimbus.model.DetailedProfile
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    app: Application,
    val apiProvider: ApiProvider = ApiProvider(ServerRepository(app), LoginRepository(app)),
) : BaseViewModel(app) {
    var supervisors: Set<Supervisor> = setOf()
    var currentUser: DetailedProfile? = null
    var windowSizeClass: WindowSizeClass? = null
    val db: AppDatabase = Room.databaseBuilder(app.applicationContext, AppDatabase::class.java, "nimbus_db")
        .build()
}