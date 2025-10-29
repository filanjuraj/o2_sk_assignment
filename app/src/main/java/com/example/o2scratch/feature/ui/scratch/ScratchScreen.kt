package com.example.o2scratch.feature.ui.scratch

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
import com.example.o2scratch.feature.presentation.scratch.ScratchScreenViewModel
import com.example.o2scratch.feature.ui.common.card.ScratchCard

@Composable
fun ScratchScreen(
    state: ScratchCardState,
    onAction: (ScratchScreenViewModel.Action) -> Unit,
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

        if (state !is ScratchCardState.Loading) {
            Button(
                onClick = { onAction(ScratchScreenViewModel.Action.Scratch) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Scratch card")
            }
        }
    }
}

