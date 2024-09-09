package com.morpho.app.model.uistate

import androidx.compose.runtime.Immutable
import com.morpho.app.CommonParcelable
import com.morpho.app.CommonParcelize
import com.morpho.app.util.JavaSerializable
import kotlinx.serialization.Serializable

@CommonParcelize
@Immutable
@Serializable
sealed interface UiLoadingState: CommonParcelable, JavaSerializable {
    data object Loading : UiLoadingState
    data object Idle : UiLoadingState
    data class Error(val errorMessage: String) : UiLoadingState
}

@CommonParcelize
@Immutable
@Serializable
sealed interface ContentLoadingState: CommonParcelable, JavaSerializable {
    data object Loading : ContentLoadingState
    data object Idle : ContentLoadingState
    data class Error(val errorMessage: String) : ContentLoadingState
}