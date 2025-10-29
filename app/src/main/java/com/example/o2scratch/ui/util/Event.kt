package com.example.o2scratch.ui.util

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.o2scratch.feature.presentation.base.ScreenEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@Composable
fun ObserveScreenEvents(
    flow: Flow<ScreenEvent>,
    snackbarState: SnackbarHostState,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                flow.collect { event ->
                    when (event) {
                        is ScreenEvent.Toast -> {
                            snackbarState.showSnackbar(message = event.message)
                        }
                    }
                }
            }
        }
    }
}