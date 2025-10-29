package com.example.o2scratch.ui.navigation

sealed interface Screen {
    val route: String

    data object Main : Screen {
        override val route: String = "main"
    }
    data object Scratch : Screen {
        override val route: String = "scratch"
    }
    data object Activation : Screen {
        override val route: String = "activation"
    }
}