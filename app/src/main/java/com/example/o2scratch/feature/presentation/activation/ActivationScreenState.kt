package com.example.o2scratch.feature.presentation.activation

import com.example.o2scratch.feature.presentation.common.ScratchCardState

data class ActivationScreenState(
    val scratchCardState: ScratchCardState,
    val showErrorDialog: Boolean,
)
