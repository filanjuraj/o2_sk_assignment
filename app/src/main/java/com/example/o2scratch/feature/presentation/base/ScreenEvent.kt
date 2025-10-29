package com.example.o2scratch.feature.presentation.base

sealed interface ScreenEvent {
    data class Toast(
        val message: String,
    ) : ScreenEvent
}