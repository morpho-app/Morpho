package com.morpho.app.di

import com.morpho.app.data.PreferencesRepository
import com.morpho.app.model.uidata.BskyDataService
import com.morpho.app.model.uidata.BskyNotificationService
import com.morpho.app.model.uidata.ContentLabelService
import com.morpho.app.screens.base.BaseScreenModel
import com.morpho.app.screens.login.LoginScreenModel
import com.morpho.app.screens.main.MainScreenModel
import com.morpho.app.screens.main.tabbed.TabbedMainScreenModel
import com.morpho.app.screens.notifications.TabbedNotificationScreenModel
import com.morpho.app.screens.profile.TabbedProfileViewModel
import com.morpho.app.util.ClipboardManager
import com.morpho.butterfly.Butterfly
import com.morpho.butterfly.auth.SessionRepository
import com.morpho.butterfly.auth.UserRepository
import com.morpho.butterfly.auth.UserRepositoryImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single<BaseScreenModel> { BaseScreenModel() }
    factory<MainScreenModel> { MainScreenModel() }
    factory<TabbedMainScreenModel> { TabbedMainScreenModel() }
    factory<TabbedProfileViewModel> { TabbedProfileViewModel() }
    factory<TabbedNotificationScreenModel> { TabbedNotificationScreenModel() }
    factory<LoginScreenModel> { LoginScreenModel() }
    factory<UpdateTick> { p-> UpdateTick(p.get<Long>()) }
    single<ClipboardManager> { ClipboardManager }
}

val storageModule = module {
    single<SessionRepository> { p-> SessionRepository(p.get()) }
    single<PreferencesRepository> { p-> PreferencesRepository(p.get())}
    singleOf(::UserRepositoryImpl) bind UserRepository::class
}

val dataModule = module {
    single<Butterfly> { Butterfly() }
    single<BskyDataService> { BskyDataService() }
    single<BskyNotificationService> { BskyNotificationService() }
    single<ContentLabelService> { ContentLabelService() }
}

@Suppress("MemberVisibilityCanBePrivate")
public class UpdateTick(val millis: Long) {
    private val _t = MutableSharedFlow<Unit>()
    val t: SharedFlow<Unit>
        get() = _t.asSharedFlow()

    public suspend fun tick(immediate: Boolean = false) {
        if (immediate) { _t.emit(Unit) }
        while (true) {
            delay(millis)
            _t.emit(Unit)
        }
    }
}