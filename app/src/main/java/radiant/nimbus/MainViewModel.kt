package radiant.nimbus

import android.app.Application
import android.content.SharedPreferences
import radiant.nimbus.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import radiant.nimbus.session.BlueskySession
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    app: Application,
    private val sharedPreferences: SharedPreferences,
    //val user: BlueskySession
) : BaseViewModel(app) {
}