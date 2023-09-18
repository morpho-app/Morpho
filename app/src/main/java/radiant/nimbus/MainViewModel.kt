package radiant.nimbus

import android.app.Application
import dagger.hilt.android.lifecycle.HiltViewModel
import radiant.nimbus.api.ApiProvider
import radiant.nimbus.api.ServerRepository
import radiant.nimbus.api.auth.LoginRepository
import radiant.nimbus.app.Supervisor
import radiant.nimbus.base.BaseViewModel
import sh.christian.ozone.api.AtIdentifier
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    app: Application,
    val apiProvider: ApiProvider = ApiProvider(ServerRepository(app), LoginRepository(app))
) : BaseViewModel(app) {
    var supervisors: Set<Supervisor> = setOf()
    var currentUser: AtIdentifier? = null
}