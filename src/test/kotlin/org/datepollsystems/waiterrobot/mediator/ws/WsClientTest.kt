package org.datepollsystems.waiterrobot.mediator.ws

import io.ktor.util.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.api.AuthApi
import org.datepollsystems.waiterrobot.mediator.api.createClient
import org.datepollsystems.waiterrobot.mediator.app.Config
import org.datepollsystems.waiterrobot.mediator.app.Settings
import org.datepollsystems.waiterrobot.mediator.ws.messages.HelloMessage
import org.datepollsystems.waiterrobot.mediator.ws.messages.HelloMessageResponse
import org.junit.Test
import kotlin.random.Random
import kotlin.test.assertEquals

internal class WsClientTest {

    /** E2E test */
    @Test
    fun basicFunctionalityTest(): Unit = runBlocking {
        if (Config.isCI) return@runBlocking // Can not be executed by CI

        val successJob = Job()
        // Make sure that the client is exited as expected and the coroutine ends
        val response = withTimeoutOrNull(10000) {
            val randomVerifier = Random.nextBytes(64).encodeBase64()

            // Login
            val tokens = AuthApi(createClient()).login("admin@admin.org", "admin")
            Settings.accessToken = tokens.accessToken
            Settings.refreshToken = tokens.refreshToken!!
            Settings.organisationId = 1

            App.socketManager.handle<HelloMessageResponse> {
                assertEquals("Hello $randomVerifier", it.body.text)
                App.socketManager.close()
                successJob.complete()
            }

            App.socketManager.addRegisterMessage(HelloMessage(randomVerifier))

            // Keeps running till wsClient.stop() is called or timeout occurs
            successJob.join()
            "success"
        }

        assertEquals("success", response, "Timed out: Handle was not called")
    }
}