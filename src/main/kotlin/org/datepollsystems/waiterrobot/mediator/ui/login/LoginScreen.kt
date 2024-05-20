package org.datepollsystems.waiterrobot.mediator.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.datepollsystems.waiterrobot.mediator.ui.common.LoadableScreen
import org.datepollsystems.waiterrobot.mediator.ui.common.autofill
import org.datepollsystems.waiterrobot.mediator.ui.common.onEnterKeyDown

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen(vm: LoginViewModel) {
    val state = vm.state.collectAsState().value

    LoadableScreen(state.screenState) {
        var mail by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.padding(50.dp).requiredWidthIn(max = 500.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(25.dp, Alignment.CenterVertically)
        ) {
            /* TODO
            Image(
                painter = painterResource(""),
                contentDescription = "WaiterRobot icon",
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .aspectRatio(1f)
                    .padding(bottom = 20.dp)
            ) */
            Text(text = "Mediator Login", style = MaterialTheme.typography.h4)
            Text(
                modifier = Modifier.padding(horizontal = 20.dp),
                textAlign = TextAlign.Center,
                text = "Please login with you WaiterRobot admin account"
            )

            if (state.loginErrorMessage != null) {
                Card(
                    shape = RoundedCornerShape(10),
                    border = BorderStroke(2.dp, Color.Red.copy(0.8f)),
                    backgroundColor = Color.Red.copy(0.5f),
                ) {
                    Box(modifier = Modifier.padding(vertical = 20.dp, horizontal = 50.dp)) {
                        Text(state.loginErrorMessage)
                    }
                }
            }

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .autofill(AutofillType.Username, AutofillType.EmailAddress) { mail = it },
                value = mail,
                isError = state.loginErrorMessage != null,
                singleLine = true,
                onValueChange = { mail = it },
                label = { Text(text = "Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .onEnterKeyDown { vm.doLogin(mail, password) }
                    .autofill(AutofillType.Password) { password = it },
                value = password,
                singleLine = true,
                isError = state.loginErrorMessage != null,
                onValueChange = { password = it },
                label = { Text(text = "Password") },
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                keyboardActions = KeyboardActions(onDone = { vm.doLogin(mail, password) }),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff

                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, description)
                    }
                },
            )

            OutlinedButton(
                onClick = { vm.doLogin(mail, password) }
            ) {
                Text(text = "Login")
            }
        }
    }
}
