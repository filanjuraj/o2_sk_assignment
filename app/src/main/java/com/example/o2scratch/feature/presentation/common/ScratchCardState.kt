package com.example.o2scratch.feature.presentation.common

sealed interface ScratchCardState {
    data object Loading : ScratchCardState
    data object Unscratched : ScratchCardState
    data class Scratched(val code: String) : ScratchCardState
    data class Activated(val code: String) : ScratchCardState
}
