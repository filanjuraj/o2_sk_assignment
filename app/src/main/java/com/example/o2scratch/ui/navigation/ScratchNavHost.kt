package com.example.o2scratch.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.o2scratch.feature.presentation.activation.ActivationScreenViewModel
import com.example.o2scratch.feature.presentation.main.MainScreenViewModel
import com.example.o2scratch.feature.presentation.scratch.ScratchScreenViewModel
import com.example.o2scratch.feature.ui.activation.ActivationScreen
import com.example.o2scratch.feature.ui.main.MainScreen
import com.example.o2scratch.feature.ui.scratch.ScratchScreen
import com.example.o2scratch.ui.util.BaseScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun ScratchNavHost(
    modifier: Modifier,
    snackbarState: SnackbarHostState,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route,
        modifier = modifier
    ) {
        composable(Screen.Main.route) {
            BaseScreen(
                viewModel = koinViewModel<MainScreenViewModel>(),
                snackbarState = snackbarState,
            ) { state, onAction ->
                MainScreen(
                    state = state,
                    onNavigateToScratch = { navController.navigate(Screen.Scratch.route) },
                    onNavigateToActivation = { navController.navigate(Screen.Activation.route) },
                )
            }
        }
        composable(Screen.Scratch.route) {
            BaseScreen(
                viewModel = koinViewModel<ScratchScreenViewModel>(),
                snackbarState = snackbarState,
            ) { state, onAction ->
                ScratchScreen(
                    state = state,
                    onAction = onAction,
                )
            }
        }
        composable(Screen.Activation.route) {
            BaseScreen(
                viewModel = koinViewModel<ActivationScreenViewModel>(),
                snackbarState = snackbarState,
            ) { state, onAction ->
                ActivationScreen(
                    state = state,
                    onAction = onAction,
                )
            }
        }
    }
}