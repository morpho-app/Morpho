
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Rectangle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.util.StatusPrinter
import com.morpho.app.App
import com.morpho.app.data.PreferencesRepository
import com.morpho.app.di.appModule
import com.morpho.app.di.dataModule
import com.morpho.app.di.storageModule
import com.morpho.app.ui.theme.MorphoTheme
import com.morpho.butterfly.auth.SessionRepository
import com.morpho.butterfly.auth.UserRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import morpho.composeapp.generated.resources.Res
import morpho.composeapp.generated.resources.morpho_icon_transparent
import net.harawata.appdirs.AppDirsFactory
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.parameter.parametersOf
import org.lighthousegames.logging.*
import org.slf4j.LoggerFactory
import kotlin.io.path.createDirectories

val log = logging("main")

@OptIn(KoinExperimentalAPI::class, ExperimentalResourceApi::class)
fun main() = application {
    //val logger: Logger = LoggerFactory.getLogger("main")
    StatusPrinter.print(LoggerFactory.getILoggerFactory() as LoggerContext)
    KmLogging.setLoggers(PlatformLogger(VariableLogLevel(LogLevel.Verbose)))
    val koin = startKoin {
        printLogger(Level.DEBUG)
        modules(appModule, storageModule, dataModule)
    }.koin
    val storageDir = AppDirsFactory.getInstance()
        .getUserDataDir("com.morpho.app", "0.1.0", "Morpho")
    val path = storageDir.toPath()
    path.toNioPath().createDirectories()
    val cacheDir = AppDirsFactory.getInstance()
        .getUserCacheDir("com.morpho.app", "0.1.0", "Morpho")
    val cachePath = cacheDir.toPath()
    cachePath.toNioPath().createDirectories()
    koin.get<SessionRepository> { parametersOf(storageDir) }
    koin.get<UserRepository> { parametersOf(storageDir) }
    val prefs = koin.get<PreferencesRepository> { parametersOf(storageDir) }
    //koin.get<Butterfly>()
    val morphoPrefs = runBlocking {
        prefs.prefs.firstOrNull()?.firstOrNull()?.morphoPrefs
    }
    val (undecorated, tabbed) = if (morphoPrefs != null) {
        log.d{ "Morpho Preferences: $morphoPrefs" }
        morphoPrefs.tabbed to morphoPrefs.undecorated
    } else {
        log.d {"No Morpho Preferences found, using defaults" }
        true to true
    }
    val windowState = rememberWindowState(
        placement = WindowPlacement.Floating,
        size = if(tabbed) DpSize(500.dp,1000.dp) else DpSize(1500.dp, 1000.dp)
    )

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "Morpho",
        undecorated = undecorated,
        transparent = undecorated,
        icon = painterResource(Res.drawable.morpho_icon_transparent)
    ) {
        MorphoTheme(darkTheme = isSystemInDarkTheme()) {
            if(undecorated) {
                MorphoWindow(
                    windowState = windowState,
                    onCloseRequest = ::exitApplication
                ) {
                    App()
                }
            } else {
                App()
            }
        }

    }
}

/*
fun isSystemInDarkTheme(): Boolean {
    return when {
        IS_WINDOWS -> {
            val result = getWindowsRegistryEntry(
                "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                "AppsUseLightTheme")
            result == 0x0
        }
        IS_MACOS -> {
            val result = getDefaultsEntry("AppleInterfaceStyle")
            result == "Dark"
        }
        else -> {
            // Probably Linux, let's check the environment
            // Once we figure out how...
            true
            // just default to dark mode for now
        }
    }
}*/

@OptIn(ExperimentalResourceApi::class)
@Composable
fun WindowScope.MorphoWindow(
    windowState: WindowState = rememberWindowState(),
    onCloseRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = if(windowState.placement == WindowPlacement.Floating) {
            Modifier.fillMaxSize().padding(5.dp)
                .shadow(3.dp, RoundedCornerShape(20.dp))
        } else Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        shape = if(windowState.placement == WindowPlacement.Floating) {
            RoundedCornerShape(20.dp)
        } else RectangleShape //window has round corners now
    ) {
        Column {
            WindowDraggableArea {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    color = MaterialTheme.colorScheme.surfaceDim,
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { },
                            modifier = Modifier.padding(5.dp).size(30.dp)
                        ) {
                            Image(
                                imageResource(Res.drawable.morpho_icon_transparent),
                                "Morpho",
                                modifier = Modifier.padding(3.dp)
                            )
                        }
                        DisableSelection {
                            Text(
                                "Morpho",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(5.dp)
                            )
                        }
                        Row(
                            //Modifier.padding(vertical = 5.dp).padding(end = 5.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    windowState.isMinimized = true
                                },
                                modifier = Modifier.padding(5.dp).size(30.dp)
                            ) {
                                Icon(
                                    Icons.Default.ExpandMore,
                                    "Minimize",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {
                                    windowState.placement = if (
                                        windowState.placement == WindowPlacement.Floating
                                    ) {
                                        WindowPlacement.Maximized
                                    } else {
                                        WindowPlacement.Floating
                                    }
                                },
                                modifier = Modifier.padding(5.dp).size(30.dp)
                            ) {
                                Icon(
                                    if(windowState.placement == WindowPlacement.Floating) {
                                        Icons.Default.ExpandLess
                                    } else {
                                        Icons.Outlined.Rectangle
                                    },
                                    if(windowState.placement == WindowPlacement.Floating) {
                                        "Maximize"
                                    } else {
                                        "Restore"
                                    },
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            FilledTonalIconButton(
                                onClick = onCloseRequest,
                                modifier = Modifier.padding(5.dp).size(30.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error,
                                    containerColor = MaterialTheme.colorScheme.surfaceDim
                                )
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    "Close",
                                    //tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }



                    }
                }
            }
            content()
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun WindowScope.MorphoDialog(
    title: String,
    windowState: DialogState = rememberDialogState(),
    onCloseRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier =
            Modifier.fillMaxSize().padding(5.dp)
                .shadow(3.dp, RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column {
            WindowDraggableArea {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    color = MaterialTheme.colorScheme.surfaceDim,
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { },
                            modifier = Modifier.padding(5.dp).size(30.dp)
                        ) {
                            Image(
                                imageResource(Res.drawable.morpho_icon_transparent),
                                "Morpho",
                                modifier = Modifier.padding(3.dp)
                            )
                        }
                        DisableSelection{
                            Text(
                                title,
                                maxLines = 1,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(5.dp).widthIn(max = windowState.size.width - 100.dp)
                            )
                        }



                        FilledTonalIconButton(
                            onClick = onCloseRequest,
                            modifier = Modifier.padding(5.dp).size(30.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                                containerColor = MaterialTheme.colorScheme.surfaceDim
                            )
                        ) {
                            Icon(
                                Icons.Default.Close,
                                "Close",
                                //tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                    }
                }
            }
            content()
        }
    }
}