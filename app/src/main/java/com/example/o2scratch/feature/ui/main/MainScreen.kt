package com.example.o2scratch.feature.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.o2scratch.feature.presentation.common.ScratchCardState
import com.example.o2scratch.feature.ui.common.card.ScratchCard
import com.example.o2scratch.ui.theme.O2ScratchTheme
import com.example.o2scratch.ui.util.ScratchPreview

@Composable
fun MainScreen(
    state: ScratchCardState,
    onNavigateToScratch: () -> Unit,
    onNavigateToActivation: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScratchCard(
            state = state,
        )

        Button(
            onClick = onNavigateToScratch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Go to Scratch Screen")
        }

        Button(
            onClick = onNavigateToActivation,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Go to Activation Screen")
        }
    }
}

@ScratchPreview
@Composable
private fun MainScreenPreview() = O2ScratchTheme {
    MainScreen(
        state = ScratchCardState.Loading,
        onNavigateToScratch = {},
        onNavigateToActivation = {}
    )
}
