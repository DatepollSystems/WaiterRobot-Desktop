package org.datepollsystems.waiterrobot.mediator.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize

@Composable
fun <T : Any> DropDownInput(
    modifier: Modifier = Modifier,
    label: String,
    items: List<T>,
    onSelectionChange: (T) -> Unit,
    placeHolderText: String,
    selectedOptionText: String?,
    content: @Composable (T) -> Unit
) {
    var expanded: Boolean by remember { mutableStateOf(false) }
    var textFieldSize by remember {
        mutableStateOf(Size.Zero)
    } // This value is used to assign the DropDown the same width

    val icon = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown

    Column(modifier = modifier) {
        OutlinedTextField(
            value = selectedOptionText ?: placeHolderText,
            onValueChange = { /* Do nothing */ },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .onGloballyPositioned { coordinates -> textFieldSize = coordinates.size.toSize() },
            readOnly = true,
            enabled = false,
            label = { Text(label) },
            trailingIcon = {
                Icon(icon, "contentDescription", Modifier.clickable { expanded = !expanded })
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
        ) {
            items.forEach {
                DropdownMenuItem(
                    text = { content(it) },
                    onClick = {
                        expanded = false
                        onSelectionChange(it)
                    }
                )
            }
        }
    }
}
