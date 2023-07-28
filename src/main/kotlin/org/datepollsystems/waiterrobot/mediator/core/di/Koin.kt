package org.datepollsystems.waiterrobot.mediator.core.di

import androidx.compose.runtime.Composable
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import org.datepollsystems.waiterrobot.mediator.data.di.dataModule
import org.koin.compose.getKoin
import org.koin.core.Koin
import org.koin.core.context.startKoin

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
