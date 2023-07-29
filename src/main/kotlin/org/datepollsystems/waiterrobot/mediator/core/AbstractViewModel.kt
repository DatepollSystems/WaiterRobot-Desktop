package org.datepollsystems.waiterrobot.mediator.core

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.datepollsystems.waiterrobot.mediator.core.di.injectLoggerForClass
import org.datepollsystems.waiterrobot.mediator.data.api.ApiException
import org.datepollsystems.waiterrobot.mediator.navigation.Navigator
import org.datepollsystems.waiterrobot.mediator.navigation.Screen
import org.koin.core.component.KoinComponent

abstract class AbstractViewModel<T : State<T>>(
    protected val navigator: Navigator,
    init: T
) : KoinComponent, ViewModel() {
    protected val logger by injectLoggerForClass()
    private val _stateFlow = MutableStateFlow(init)
    val state: StateFlow<T> = _stateFlow

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        when (exception) {
            is ApiException.AppVersionTooOld -> navigator.navigate(Screen.AppVersionTooOld)

            else -> {
                logger.e(exception) {
                    "Unhandled exception in intent. Exceptions should be handled directly in the intent!"
                }
                reduceError("Fehler", "Etwas ist schief gelaufen. Bitte versuche es erneut.")
            }
        }
    }

    init {
        inVmScope { onCreate() }
    }

    protected open suspend fun onCreate() {
        // Default do nothing
    }

    fun reduce(reducer: T.() -> T) {
        _stateFlow.update(reducer)
    }

    fun <T> inVmScope(block: suspend () -> T) {
        viewModelScope.launch(SupervisorJob() + exceptionHandler) {
            block()
        }
    }

    protected fun reduceError(
        errorTitle: String,
        errorMsg: String,
        dismiss: () -> Unit = this@AbstractViewModel::dismissError
    ) = reduce {
        withScreenState(ScreenState.Error(errorTitle, errorMsg, dismiss))
    }

    private fun dismissError() = inVmScope {
        reduce {
            withScreenState(ScreenState.Idle)
        }
    }
}

interface State<out S : State<S>> {
    val screenState: ScreenState

    fun withScreenState(screenState: ScreenState): S
}

sealed class ScreenState {
    object Idle : ScreenState()
    object Loading : ScreenState()
    data class Error(val title: String, val message: String, val onDismiss: () -> Unit) : ScreenState()
}

object EmptyState : State<EmptyState> {
    override val screenState = ScreenState.Idle
    override fun withScreenState(screenState: ScreenState): EmptyState = this
}