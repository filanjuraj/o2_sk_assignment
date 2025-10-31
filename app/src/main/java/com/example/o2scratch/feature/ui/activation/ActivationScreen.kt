package com.example.o2scratch.feature.ui.activation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.o2scratch.feature.presentation.activation.ActivationScreenState
import com.example.o2scratch.feature.presentation.activation.ActivationScreenViewModel
import com.example.o2scratch.feature.presentation.common.ScratchCardState
import com.example.o2scratch.feature.ui.common.card.ScratchCard
import com.example.o2scratch.ui.util.ScratchPreview

@Composable
fun ActivationScreen(
    state: ActivationScreenState,
    onAction: (ActivationScreenViewModel.Action) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScratchCard(
            state = state.scratchCardState,
        )

        if (state.scratchCardState !is ScratchCardState.Loading) {
            Button(
                onClick = { onAction(ActivationScreenViewModel.Action.Activate) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Activate card")
            }
        }
    }

    if (state.showErrorDialog) {
        ErrorDialog(
            onDismiss = { onAction(ActivationScreenViewModel.Action.DismissErrorDialog) }
        )
    }
}

@Composable
private fun ErrorDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text(
                text = "⚠️",
                style = MaterialTheme.typography.displayMedium
            )
        },
        title = {
            Text(
                text = "Version Too Old",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "The version of this card is too old and cannot be activated. Please contact support for assistance.",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@ScratchPreview
@Composable
private fun ActivationScreenPreview() {
    ActivationScreen(
        state = ActivationScreenState(
            scratchCardState = ScratchCardState.Scratched(code = "ABC123"),
            showErrorDialog = false,
        ),
        onAction = {}
    )
}

@ScratchPreview
@Composable
private fun ActivationScreenLoadingPreview() {
    ActivationScreen(
        state = ActivationScreenState(
            scratchCardState = ScratchCardState.Loading,
            showErrorDialog = false,
        ),
        onAction = {}
    )
}

@ScratchPreview
@Composable
private fun ActivationScreenErrorDialogPreview() {
    ActivationScreen(
        state = ActivationScreenState(
            scratchCardState = ScratchCardState.Scratched(code = "ABC123"),
            showErrorDialog = true,
        ),
        onAction = {}
    )
}

