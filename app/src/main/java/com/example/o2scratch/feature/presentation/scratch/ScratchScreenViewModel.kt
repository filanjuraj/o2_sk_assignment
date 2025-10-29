package com.example.o2scratch.feature.presentation.scratch

import androidx.lifecycle.viewModelScope
import com.example.o2scratch.feature.domain.repository.ScratchCardRepository
import com.example.o2scratch.feature.presentation.base.BaseViewModel
import com.example.o2scratch.feature.presentation.base.ScreenEvent
import com.example.o2scratch.feature.presentation.common.ScratchCardState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScratchScreenViewModel(
    private val scratchCardRepository: ScratchCardRepository,
) : BaseViewModel<ScratchCardState, ScratchScreenViewModel.Action>(
    initialState = ScratchCardState.Loading
) {

    init {
        viewModelScope.launch {
            scratchCardRepository.cardState.collect { cardState ->
                currentState = cardState
            }
        }
    }

    override fun onAction(action: Action) {
        when (action) {
            Action.Scratch -> {
                onScratchAction()
            }
        }
    }

    private fun onScratchAction() {
        when (currentState) {
            is ScratchCardState.Unscratched -> {
                scratchCard()
            }

            is ScratchCardState.Scratched -> {
                sendEvent(ScreenEvent.Toast("Card is already scratched"))
            }

            is ScratchCardState.Activated -> {
                sendEvent(ScreenEvent.Toast("Card is already activated"))
            }

            else -> {
                sendEvent(ScreenEvent.Toast("Could not scratch the card. Please try again later."))
            }
        }
    }

    private fun scratchCard() {
        viewModelScope.launch(Dispatchers.IO) {
            scratchCardRepository.scratchCard()
        }
    }

    sealed interface Action {
        data object Scratch : Action
    }
}