package radiant.nimbus

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceDataStore
import radiant.nimbus.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import radiant.nimbus.session.BlueskySession
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    app: Application,
    userData: PreferenceDataStore
) : BaseViewModel(app) {
}