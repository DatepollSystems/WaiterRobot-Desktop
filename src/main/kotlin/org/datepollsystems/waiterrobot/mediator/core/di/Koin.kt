package org.datepollsystems.waiterrobot.mediator.core.di

import androidx.compose.runtime.Composable
import co.touchlab.kermit.Logger
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import org.datepollsystems.waiterrobot.mediator.data.di.dataModule
import org.koin.compose.getKoin
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope

fun initKoin() = startKoin {
    modules(
        coreModule,
        dataModule
    )
}

@Composable
inline fun <reified T : ViewModel> getViewModel(
    key: Any = T::class,
    crossinline builder: Koin.() -> T
): T {
    val koin = getKoin()
    return getViewModel(
        key = key,
        klass = T::class,
        viewModelBlock = { builder(koin) }
    )
}

fun Scope.getLogger(tag: String): Logger = get { parametersOf(tag) }
fun KoinComponent.injectLogger(tag: String): Lazy<Logger> = inject { parametersOf(tag) }
internal fun KoinComponent.injectLoggerForClass(): Lazy<Logger> =
    injectLogger(this::class.simpleName ?: "AnonymousClass")
