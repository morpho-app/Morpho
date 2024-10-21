package com.morpho.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.morpho.app.data.MorphoAgent
import com.morpho.app.ui.elements.SettingsGroup
import com.morpho.app.ui.elements.SettingsItem
import com.morpho.butterfly.Language
import org.koin.compose.getKoin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettings(
    agent: MorphoAgent = getKoin().get(),
    modifier: Modifier = Modifier,
    distinguish: Boolean = true,
    topLevel: Boolean = true,
) {
    val morphoPrefs = agent.morphoPrefs.value
    SettingsGroup(
        title = if(!topLevel) "Language Settings" else "",
        modifier = modifier,
        distinguish = distinguish,
    ) {
        SettingsItem(text = AnnotatedString("App Language")) {
            LanguageDropDownMenu(
                onSelected = { lang ->
                    agent.setUILanguage(lang)
                },
                initialLanguage = morphoPrefs.uiLanguage ?: agent.myLanguage.value
            )
        }
    }
}

@Composable
fun LanguageDropDownMenu(
    onSelected: (Language) -> Unit,
    initialLanguage: Language,
    expandedInitially: Boolean = false,
) {
    Box(Modifier.height(100.dp).fillMaxWidth()) {
        val shape = MaterialTheme.shapes.medium
        var expanded by remember { mutableStateOf(expandedInitially) }
        var language by remember { mutableStateOf(initialLanguage) }
        val onItemClicked: (Language) -> Unit = { lang ->
            language = lang
            onSelected(lang)
            expanded = false
        }
        Button(
            onClick = { expanded = !expanded },
            shape = shape,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(12.dp)
            ),
            modifier = Modifier.width(240.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(start = 12.dp, top = 10.dp, bottom = 10.dp)
            ) {
                Text(
                    initialLanguage.toLanguageName()
                )
                Spacer(Modifier.width(4.dp).weight(1f))
                Icon(
                    Icons.Rounded.KeyboardArrowDown,
                    null,
                )
            }
        }
        DropdownMenu(modifier = Modifier.align(Alignment.TopCenter).width(240.dp), expanded = expanded,
            onDismissRequest = { expanded = false }) {

            DropdownMenuItem(text = { Text(Language("en").toLanguageName()) }, onClick = {
                onItemClicked(Language("en"))
            })
            DropdownMenuItem(text = { Text(Language("pt").toLanguageName()) }, onClick = {
                onItemClicked(Language("pt"))
            })
            DropdownMenuItem(text = { Text(Language("fr").toLanguageName()) }, onClick = {
                onItemClicked(Language("fr"))
            })
            DropdownMenuItem(text = { Text(Language("es").toLanguageName()) }, onClick = {
                onItemClicked(Language("es"))
            })
            DropdownMenuItem(text = { Text(Language("de").toLanguageName()) }, onClick = {
                onItemClicked(Language("de"))
            })
            DropdownMenuItem(text = { Text(Language("ar").toLanguageName()) }, onClick = {
                onItemClicked(Language("ar"))
            })
            DropdownMenuItem(text = { Text(Language("tr").toLanguageName()) }, onClick = {
                onItemClicked(Language("tr"))
            })
            DropdownMenuItem(text = { Text(Language("ru").toLanguageName()) }, onClick = {
                onItemClicked(Language("ru"))
            })
            DropdownMenuItem(text = { Text(Language("it").toLanguageName()) }, onClick = {
                onItemClicked(Language("it"))
            })
            DropdownMenuItem(text = { Text(Language("ja").toLanguageName()) }, onClick = {
                onItemClicked(Language("ja"))
            })
            DropdownMenuItem(text = { Text(Language("ko").toLanguageName()) }, onClick = {
                onItemClicked(Language("ko"))
            })

        }
    }
}

fun Language.toLanguageName(): String {
    return when(this) {
        Language("en") -> "English"
        Language("pt") -> "Português - Portuguese"
        Language("fr") -> "Français - French"
        Language("es") -> "Español - Spanish"
        Language("de") -> "Deutsch - German"
        Language("ar") -> "العربية - Arabic"
        Language("tr") -> "Türkçe - Turkish"
        Language("ru") -> "Русский - Russian"
        Language("it") -> "Italiano - Italian"
        Language("ja") -> "日本語 - Japanese"
        Language("ko") -> "한국어 - Korean"
        else -> "Not handled yet"
    }
}