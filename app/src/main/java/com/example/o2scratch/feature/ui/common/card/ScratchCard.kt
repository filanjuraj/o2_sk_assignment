package com.example.o2scratch.feature.ui.common.card

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.o2scratch.feature.presentation.common.ScratchCardState
import com.example.o2scratch.ui.util.ScratchPreview

@Composable
fun ScratchCard(
    state: ScratchCardState,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        when (state) {
            is ScratchCardState.Loading -> {
                LoadingCardContent()
            }

            is ScratchCardState.Unscratched -> {
                UnstratchedCardContent()
            }

            is ScratchCardState.Scratched -> {
                ScratchedCardContent(code = state.code)
            }

            is ScratchCardState.Activated -> {
                ActivatedCardContent(code = state.code)
            }
        }
    }
}


@ScratchPreview
@Composable
private fun PreviewLoadingCard() {
    ScratchCard(state = ScratchCardState.Loading)
}

@ScratchPreview
@Composable
private fun PreviewUnstratchedCard() {
    ScratchCard(state = ScratchCardState.Unscratched)
}

@ScratchPreview
@Composable
private fun PreviewScratchedCard() {
    ScratchCard(state = ScratchCardState.Scratched(code = "ABC123"))
}

@ScratchPreview
@Composable
private fun PreviewActivatedCard() {
    ScratchCard(state = ScratchCardState.Activated(code = "ABC123"))
}
