
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.util.StatusPrinter
import com.github.tkuenneth.nativeparameterstoreaccess.MacOSDefaults.getDefaultsEntry
import com.github.tkuenneth.nativeparameterstoreaccess.NativeParameterStoreAccess.IS_MACOS
import com.github.tkuenneth.nativeparameterstoreaccess.NativeParameterStoreAccess.IS_WINDOWS
import com.github.tkuenneth.nativeparameterstoreaccess.WindowsRegistry.getWindowsRegistryEntry
import com.morpho.app.App
import com.morpho.app.data.PreferencesRepository
import com.morpho.app.di.appModule
import com.morpho.app.di.dataModule
import com.morpho.app.di.storageModule
import com.morpho.app.ui.theme.DarkColorScheme
import com.morpho.app.ui.theme.LightColorScheme
import com.morpho.butterfly.auth.SessionRepository
import com.morpho.butterfly.auth.UserRepository
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
    koin.get<PreferencesRepository> { parametersOf(storageDir) }
    //koin.get<Butterfly>()
    val undecorated = true
    val colors = if(isSystemInDarkTheme()) DarkColorScheme else LightColorScheme
    Window(
        onCloseRequest = ::exitApplication,
        title = "Morpho",
        undecorated = undecorated,
        transparent = undecorated,
        icon = painterResource(Res.drawable.morpho_icon_transparent)
    ) {
        if(undecorated) {
            MaterialTheme(colorScheme = colors) {
                Surface(
                    modifier = Modifier.fillMaxSize().padding(5.dp)
                        .shadow(3.dp, RoundedCornerShape(20.dp)),
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(20.dp) //window has round corners now
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
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Image(
                                            imageResource(Res.drawable.morpho_icon_transparent),
                                            "Morpho"
                                        )
                                    }

                                    Text(
                                        "Morpho",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(5.dp)
                                    )

                                    IconButton(
                                        onClick = ::exitApplication,
                                        modifier = Modifier.padding(5.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            "Close",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                }
                            }
                        }
                        App()
                    }
                }
            }
        } else {
            App()
        }

    }
}

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
}