package org.datepollsystems.waiterrobot.mediator.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.datepollsystems.waiterrobot.mediator.ui.common.LoadableScreen
import org.datepollsystems.waiterrobot.mediator.ui.theme.Typography
import org.datepollsystems.waiterrobot.mediator.utils.icons.filled.Visibility
import org.datepollsystems.waiterrobot.mediator.utils.icons.filled.VisibilityOff

@Composable
fun LoginScreen(vm: LoginViewModel) {

    val state = vm.state.collectAsState().value

    LoadableScreen(state.screenState) {
        var mail by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.fillMaxSize().padding(50.dp),
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
            Text(text = "Mediator Login", style = Typography.h4)
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
                modifier = Modifier.fillMaxWidth(),
                value = mail,
                onValueChange = { mail = it },
                label = { Text(text = "Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = password,
                onValueChange = { password = it },
                label = { Text(text = "Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff

                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, description)
                    }
                }
            )
            OutlinedButton(onClick = { vm.doLogin(mail, password) }) {
                Text(text = "Login")
            }
        }
    }
}