package com.example.o2scratch.feature.presentation.main

import androidx.lifecycle.viewModelScope
import com.example.o2scratch.feature.domain.repository.ScratchCardRepository
import com.example.o2scratch.feature.presentation.base.BaseViewModel
import com.example.o2scratch.feature.presentation.common.ScratchCardState
import kotlinx.coroutines.launch

class MainScreenViewModel(
    private val scratchCardRepository: ScratchCardRepository,
) : BaseViewModel<ScratchCardState, Unit>(ScratchCardState.Loading){

    init {
        viewModelScope.launch {
            scratchCardRepository.cardState.collect { cardState ->
                currentState = cardState
            }
        }
    }

    override fun onAction(action: Unit) {
        // No events to handle currently
    }
}