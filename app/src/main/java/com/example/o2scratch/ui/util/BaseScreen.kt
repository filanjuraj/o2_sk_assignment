package com.example.o2scratch.ui.util

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.o2scratch.feature.presentation.base.BaseViewModel

@Composable
fun <VM : BaseViewModel<State, Action>, State : Any, Action : Any> BaseScreen(
    viewModel: VM,
    snackbarState: SnackbarHostState,
    Screen: @Composable (state: State, onAction: (Action) -> Unit) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveScreenEvents(viewModel.events, snackbarState)

    Screen(state, viewModel::onAction)
}