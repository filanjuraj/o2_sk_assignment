package com.example.o2scratch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.o2scratch.ui.navigation.ScratchNavHost
import com.example.o2scratch.ui.theme.O2ScratchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            O2ScratchTheme {
                val snackbarState = remember {
                    SnackbarHostState()
                }

                Scaffold(
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarState)
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    ScratchNavHost(
                        modifier = Modifier.padding(innerPadding),
                        snackbarState = snackbarState
                    )
                }
            }
        }
    }
}
