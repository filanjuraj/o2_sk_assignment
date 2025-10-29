package com.example.o2scratch.feature.presentation.activation

import androidx.lifecycle.viewModelScope
import com.example.o2scratch.feature.data.networking.ActivationException
import com.example.o2scratch.feature.domain.repository.ScratchCardRepository
import com.example.o2scratch.feature.presentation.base.BaseViewModel
import com.example.o2scratch.feature.presentation.base.ScreenEvent
import com.example.o2scratch.feature.presentation.common.ScratchCardState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivationScreenViewModel(
    private val scratchCardRepository: ScratchCardRepository,
) : BaseViewModel<ActivationScreenState, ActivationScreenViewModel.Action>(
    initialState = ActivationScreenState(scratchCardState = ScratchCardState.Loading, showErrorDialog = false)
) {

    init {
        viewModelScope.launch {
            scratchCardRepository.cardState.collect { cardState ->
                currentState = currentState.copy(scratchCardState = cardState)
            }
        }
    }

    override fun onAction(action: Action) {
        when (action) {
            Action.Activate -> {
                onActivateAction()
            }
            Action.DismissErrorDialog -> {
                currentState = currentState.copy(showErrorDialog = false)
            }
        }
    }

    private fun onActivateAction() {
        when (val scratchCardState = currentState.scratchCardState) {
            is ScratchCardState.Scratched -> {
                currentState = currentState.copy(scratchCardState = ScratchCardState.Loading)
                activateCard(scratchCardState)
            }
            is ScratchCardState.Activated -> {
                sendEvent(ScreenEvent.Toast("Card is already activated"))
            }
            else -> {
                sendEvent(ScreenEvent.Toast("You need to scratch the card first"))
            }
        }
    }

    private fun activateCard(scratchCardState: ScratchCardState.Scratched) {
        viewModelScope.launch(Dispatchers.IO) {
            scratchCardRepository.activateCard(scratchCardState.code).fold(
                onSuccess = {
                    // Activation successful, state is already updated in the repository
                },
                onFailure = { exception ->
                    when (exception) {
                        is ActivationException -> {
                            currentState = currentState.copy(showErrorDialog = true)
                        }
                        else -> {
                            sendEvent(ScreenEvent.Toast("Could not activate the card. Please try again later."))
                        }
                    }
                }
            )
        }
    }

    sealed interface Action {
        data object Activate : Action
        data object DismissErrorDialog : Action
    }
}