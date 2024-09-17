package com.morpho.app.ui.elements

import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationInstance
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

class MorphoHighlightIndicationInstance(isEnabledState: State<Boolean>) :
    IndicationInstance {
    private val isEnabled by isEnabledState
    override fun ContentDrawScope.drawIndication() {
        drawContent()
        if (isEnabled) {
            drawRoundRect(cornerRadius = CornerRadius(4.dp.toPx()), size = size, color = Color.Gray, alpha = 0.2f)
            drawRoundRect(cornerRadius = CornerRadius(4.dp.toPx()),
                          style = Stroke(width = Stroke.HairlineWidth),
                          size = size, color = Color.White, alpha = 0.9f)
        }
    }

}

class MorphoHighlightIndication : Indication {
    @Composable
    override fun rememberUpdatedInstance(interactionSource: InteractionSource):
            IndicationInstance {
        val isFocusedState = interactionSource.collectIsFocusedAsState()
        return remember(interactionSource) {
            MorphoHighlightIndicationInstance(isEnabledState = isFocusedState)
        }
    }
}