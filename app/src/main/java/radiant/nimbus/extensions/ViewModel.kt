package radiant.nimbus.extensions

import androidx.activity.ComponentActivity
import androidx.annotation.MainThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.Factory
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.reflect.KClass

class LifeCycleViewModelLazy<VM>(
    private val viewModelClass: KClass<VM>,
    private val storeProducer: () -> ViewModelStore,
    private val factoryProducer: () -> Factory,
    private val extrasProducer: () -> CreationExtras = { CreationExtras.Empty },
    private val lifecycle: () -> Lifecycle
) : Lazy<VM> where VM : ViewModel, VM : LifecycleObserver {

    private var cached: VM? = null

    override val value: VM
        get() {
            val viewModel = cached
            return if (viewModel == null) {
                val factory = factoryProducer()
                val store = storeProducer()
                ViewModelProvider(
                    store,
                    factory,
                    extrasProducer()
                ).get(viewModelClass.java).also {
                    cached = it
                }
                ViewModelProvider(
                    store,
                    factory,
                ).get(viewModelClass.java).also {
                    lifecycle().addObserver(it)
                    cached = it
                }
            } else {
                viewModel
            }
        }

    override fun isInitialized(): Boolean = cached != null
}

@MainThread
inline fun <reified VM> ComponentActivity.lifecycleViewModels(
    noinline factoryProducer: (() -> Factory)? = null
): Lazy<VM> where VM : ViewModel, VM : LifecycleObserver {
    val factoryPromise = factoryProducer ?: {
        defaultViewModelProviderFactory
    }
    return LifeCycleViewModelLazy(
        VM::class,
        { viewModelStore },
        factoryPromise,
        { this.defaultViewModelCreationExtras },
        { lifecycle },
    )
}

@Composable
inline fun <reified T : ViewModel, S : ViewModelStoreOwner> viewModelInStore(store: S): Result<T> =
    runCatching {
        var result: Result<T>? = null
        CompositionLocalProvider(LocalViewModelStoreOwner provides store) {
            result = runCatching { viewModel(T::class.java) }
        }
        result!!.getOrThrow()
    }

/** Try to fetch a viewModel with current context (i.e. activity)  */
@Composable
inline fun <reified T : ViewModel> safeActivityViewModel(): Result<T> = runCatching {
    val activity = LocalContext.current as? ViewModelStoreOwner
        ?: throw IllegalStateException("Current context is not a viewModelStoreOwner.")
    return viewModelInStore(activity)
}

/** Force fetch a viewModel inside context viewModelStore */
@Composable
inline fun <reified T : ViewModel> activityViewModel(): T = safeActivityViewModel<T>().getOrThrow()