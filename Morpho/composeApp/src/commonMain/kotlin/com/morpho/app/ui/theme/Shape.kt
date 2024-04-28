package com.morpho.app.ui.theme

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val roundedTopLBotR = Shapes(
    extraSmall = ShapeDefaults.ExtraSmall.copy(topEnd = CornerSize(0.dp), bottomStart = CornerSize(0.dp)),
    small = ShapeDefaults.Small.copy(topEnd = CornerSize(0.dp), bottomStart = CornerSize(0.dp)),
    medium = ShapeDefaults.Medium.copy(topEnd = CornerSize(0.dp), bottomStart = CornerSize(0.dp)),
    large = ShapeDefaults.Large.copy(topEnd = CornerSize(0.dp), bottomStart = CornerSize(0.dp)),
    extraLarge = ShapeDefaults.ExtraLarge.copy(topEnd = CornerSize(0.dp), bottomStart = CornerSize(0.dp))
)

val roundedTopR = Shapes(
    extraSmall = ShapeDefaults.ExtraSmall.copy(
        bottomEnd = CornerSize(0.dp),
        bottomStart = CornerSize(0.dp),
        topStart = CornerSize(0.dp),
    ),
    small = ShapeDefaults.Small.copy(
        bottomEnd = CornerSize(0.dp),
        bottomStart = CornerSize(0.dp),
        topStart = CornerSize(0.dp),
    ),
    medium = ShapeDefaults.Medium.copy(
        bottomEnd = CornerSize(0.dp),
        bottomStart = CornerSize(0.dp),
        topStart = CornerSize(0.dp),
    ),
    large = ShapeDefaults.Large.copy(
        bottomEnd = CornerSize(0.dp),
        bottomStart = CornerSize(0.dp),
        topStart = CornerSize(0.dp),
    ),
    extraLarge = ShapeDefaults.ExtraLarge.copy(
        bottomEnd = CornerSize(0.dp),
        bottomStart = CornerSize(0.dp),
        topStart = CornerSize(0.dp),
    )
)

val roundedBotR = Shapes(
    extraSmall = ShapeDefaults.ExtraSmall.copy(
        topEnd = CornerSize(0.dp),
        topStart = CornerSize(0.dp),
        bottomStart = CornerSize(0.dp),
    ),
    small = ShapeDefaults.Small.copy(
        topEnd = CornerSize(0.dp),
        topStart = CornerSize(0.dp),
        bottomStart = CornerSize(0.dp),
    ),
    medium = ShapeDefaults.Medium.copy(
        topEnd = CornerSize(0.dp),
        topStart = CornerSize(0.dp),
        bottomStart = CornerSize(0.dp),
    ),
    large = ShapeDefaults.Large.copy(
        topEnd = CornerSize(0.dp),
        topStart = CornerSize(0.dp),
        bottomStart = CornerSize(0.dp),
    ),
    extraLarge = ShapeDefaults.ExtraLarge.copy(
        topEnd = CornerSize(0.dp),
        topStart = CornerSize(0.dp),
        bottomStart = CornerSize(0.dp),
    )
)