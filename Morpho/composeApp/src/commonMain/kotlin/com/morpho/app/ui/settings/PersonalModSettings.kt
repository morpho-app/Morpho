package com.morpho.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.ReduceCapacity
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.morpho.app.data.MorphoAgent
import com.morpho.app.ui.elements.SettingsGroup
import com.morpho.app.ui.elements.SettingsItem
import kotlinx.serialization.Serializable
import org.koin.compose.getKoin

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PersonalModSettings(
    agent: MorphoAgent = getKoin().get(),
    modifier: Modifier = Modifier,
    distinguish: Boolean = true,
    navigator: Navigator = LocalNavigator.currentOrThrow,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var sheetOption by remember { mutableStateOf(SheetOption.Hide) }
    SettingsGroup(
        title = "Moderation tools",
        modifier = modifier,
        distinguish = distinguish,
    ) {
        SettingsItem(
            description = AnnotatedString("Muted words and tags"),
            modifier = Modifier.clickable {
                sheetOption = SheetOption.MuteWords
            }
        ){
            Icon(
                Icons.Default.FilterAlt,
                contentDescription = "Filter",
            )
        }

        SettingsItem(
            description = AnnotatedString("Moderation lists"),
            modifier = Modifier.clickable {

            }
        ){
            Icon(
                Icons.Default.ReduceCapacity,
                contentDescription = "People",
            )
        }

        SettingsItem(
            description = AnnotatedString("Muted accounts"),
            modifier = Modifier.clickable {

            }
        ) {
            Icon(
                Icons.Default.VisibilityOff,
                contentDescription = "Mute/Hide",
            )
        }

        SettingsItem(
            description = AnnotatedString("Blocked accounts"),
            modifier = Modifier.clickable {

            }
        ){
            Icon(
                Icons.Default.Block,
                contentDescription = "Block",
            )
        }
    }
    if(sheetOption != SheetOption.Hide) {
        ModalBottomSheet(
            onDismissRequest = {
                sheetOption = SheetOption.Hide
            },
            sheetState = sheetState
        ) {
            when(sheetOption) {
                SheetOption.MuteWords -> {
                    MutedWordsSettings(
                        agent = agent,
                        scope = scope,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                SheetOption.Hide -> {}
            }
        }
    }

}

@Serializable
@Immutable
enum class SheetOption {
    MuteWords,
    Hide
}