package org.datepollsystems.waiterrobot.mediator.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.datepollsystems.waiterrobot.mediator.core.ScreenState

@Composable
fun LoadableScreen(
    screenState: ScreenState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxSize(),
    ) {
        if (screenState == ScreenState.Loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            content()
        }

        if (screenState is ScreenState.Error) {
            ErrorDialog(screenState)
        }
    }
}
