package org.datepollsystems.waiterrobot.mediator.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.datepollsystems.waiterrobot.mediator.navigation.Navigator

abstract class ViewModel<T : State>(
    protected val navigator: Navigator,
    protected val viewModelScope: CoroutineScope,
    init: T
) {
    private val _stateFlow = MutableStateFlow(init)
    val state: StateFlow<T> = _stateFlow

    init {
        inVmScope { onCreate() }
    }

    protected open suspend fun onCreate() {
        // Default do nothing
    }

    suspend fun reduce(reducer: T.() -> T) {
        val newState = reducer(state.value)
        _stateFlow.emit(newState)
    }

    fun <T> inVmScope(block: suspend () -> T) {
        viewModelScope.launch(Dispatchers.Unconfined) {
            block()
        }
    }
}

interface State {
    val screenState: ScreenState
}

sealed class ScreenState {
    object Idle : ScreenState()
    object Loading : ScreenState()
    data class Error(val message: String) : ScreenState()
}

object EmptyState : State {
    override val screenState = ScreenState.Idle
}