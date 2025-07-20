package org.datepollsystems.waiterrobot.mediator.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
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
import org.datepollsystems.waiterrobot.mediator.mediator.generated.resources.*
import org.datepollsystems.waiterrobot.mediator.ui.common.LoadableScreen
import org.datepollsystems.waiterrobot.mediator.ui.common.autofill
import org.datepollsystems.waiterrobot.mediator.ui.common.onEnterKeyDown
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen(vm: LoginViewModel) {
    val state = vm.state.collectAsState().value

    LoadableScreen(state.screenState) {
        var mail by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        val loginContainsControlChar by remember {
            derivedStateOf {
                mail.any { Character.isISOControl(it.code) } || password.any { Character.isISOControl(it.code) }
            }
        }

        Column(
            modifier = Modifier.padding(50.dp).requiredWidthIn(max = 550.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(25.dp, Alignment.CenterVertically)
        ) {
            Image(
                painter = painterResource(Res.drawable.icon_round),
                contentDescription = "WaiterRobot icon",
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .aspectRatio(1f)
                    .padding(bottom = 20.dp)
            )
            Text(text = stringResource(Res.string.login_title), style = MaterialTheme.typography.headlineMedium)
            Text(
                modifier = Modifier.padding(horizontal = 20.dp),
                textAlign = TextAlign.Center,
                text = stringResource(Res.string.login_subtitle)
            )

            if (state.loginErrorMessage != null) {
                Card(
                    shape = RoundedCornerShape(10),
                    border = BorderStroke(2.dp, Color.Red.copy(0.8f)),
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(0.5f)),
                ) {
                    Box(modifier = Modifier.padding(vertical = 20.dp, horizontal = 50.dp)) {
                        Text(stringResource(state.loginErrorMessage))
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
                label = { Text(text = stringResource(Res.string.login_email)) },
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
                label = { Text(text = stringResource(Res.string.login_password)) },
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

            if (loginContainsControlChar) {
                Card(
                    shape = RoundedCornerShape(10),
                    border = BorderStroke(2.dp, Color.Yellow.copy(0.8f)),
                    colors = CardDefaults.cardColors(containerColor = Color.Yellow.copy(0.5f)),
                ) {
                    Box(modifier = Modifier.padding(vertical = 20.dp, horizontal = 50.dp)) {
                        Text(stringResource(Res.string.login_control_char_warning))
                    }
                }
            }

            OutlinedButton(
                onClick = { vm.doLogin(mail, password) }
            ) {
                Text(text = stringResource(Res.string.login))
            }
        }
    }
}
