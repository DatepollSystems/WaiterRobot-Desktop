package org.datepollsystems.waiterrobot.mediator.ui.configurePrinters

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import org.datepollsystems.waiterrobot.mediator.api.dto.GetEventDto
import org.datepollsystems.waiterrobot.mediator.api.dto.GetOrganisationDto
import org.datepollsystems.waiterrobot.mediator.ui.common.DropDownInput
import org.datepollsystems.waiterrobot.mediator.ui.common.LoadableScreen

@Composable
fun ConfigurePrintersScreen(vm: ConfigurePrintersViewModel) {
    val state = vm.state.collectAsState().value

    LoadableScreen(state.screenState) {
        Column {
            DropDownInput("Organisation", state.availableOrganisations ?: emptyList(), GetOrganisationDto::name) {
                vm.changeOrganisation(it.id)
            }
            if (state.selectedOrganisationId != null) {
                DropDownInput("Event", state.availableEvents ?: emptyList(), GetEventDto::name) {
                    vm.changeEvent(it.id)
                }
            }
        }
    }
}