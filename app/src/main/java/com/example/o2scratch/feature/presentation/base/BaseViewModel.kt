package com.example.o2scratch.feature.presentation.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

abstract class BaseViewModel<State : Any, Action : Any>(initialState: State) : ViewModel() {

    private val _state = MutableStateFlow<State>(initialState)
    val state = _state.asStateFlow()

    private val _events = Channel<ScreenEvent>()
    val events = _events.receiveAsFlow()

    protected var currentState: State
        get() = _state.value
        set(value) {
            _state.value = value
        }

    abstract fun onAction(action: Action)

    protected fun sendEvent(event: ScreenEvent) {
        _events.trySend(event)
    }
}