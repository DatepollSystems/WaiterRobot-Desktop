package org.datepollsystems.waiterrobot.mediator.maual

import co.touchlab.kermit.Logger
import co.touchlab.kermit.loggerConfigInit
import co.touchlab.kermit.platformLogWriter
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import org.datepollsystems.waiterrobot.mediator.App
import org.datepollsystems.waiterrobot.mediator.app.Config
import org.datepollsystems.waiterrobot.mediator.app.removeLoginIdentifierEnvPrefix
import org.datepollsystems.waiterrobot.mediator.core.ID
import org.datepollsystems.waiterrobot.mediator.core.api.AuthorizedClient
import org.datepollsystems.waiterrobot.mediator.core.api.createClient
import org.datepollsystems.waiterrobot.mediator.core.di.coreModule
import org.datepollsystems.waiterrobot.mediator.data.api.AuthApi
import org.datepollsystems.waiterrobot.mediator.data.api.EventApi
import org.datepollsystems.waiterrobot.mediator.data.api.OrganisationApi
import org.datepollsystems.waiterrobot.mediator.data.api.dto.TokenDto
import org.koin.core.context.startKoin
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.seconds

// TODO add a file logger (and maybe limit the console logger to important stuff or remove compleatly?)
private val logger = Logger(loggerConfigInit(platformLogWriter()))

suspend fun main(): Unit = try {
    coroutineScope {
        startKoin { modules(coreModule) }
        val loginClient = createClient(enableNetworkLogs = true, logger = logger)

        print("Email/Username: ")
        var email = readln()
        val password = System.console()?.readPassword("Password: ")?.joinToString("") ?: run {
            print("Password: ")
            readln()
        }

        App.config = Config.getFromLoginIdentifier(email)
        email = email.removeLoginIdentifierEnvPrefix()
        if (App.config == Config.Prod) error("Testing on Prod is not possible")

        AuthApi(loginClient).login(email, password).let {
            TmpStorage.userAccessToken = it.accessToken
            TmpStorage.userRefreshToken = it.refreshToken ?: TmpStorage.userRefreshToken
        }

        val userClient = AuthorizedClient(createClient(enableNetworkLogs = true, logger = logger).config {
            install(Auth) {
                bearer {
                    loadTokens {
                        BearerTokens(TmpStorage.userAccessToken, TmpStorage.userRefreshToken)
                    }

                    refreshTokens {
                        val authApi = AuthApi(createClient(enableNetworkLogs = true, logger = logger))
                        @Suppress("TooGenericExceptionCaught")
                        try {
                            val tokenInfo = authApi.refresh(TmpStorage.userRefreshToken)

                            TmpStorage.userAccessToken = tokenInfo.accessToken
                            // Only override when got a new sessionToken
                            tokenInfo.refreshToken?.let { TmpStorage.userRefreshToken = it }

                            BearerTokens(
                                accessToken = tokenInfo.accessToken,
                                refreshToken = tokenInfo.refreshToken ?: TmpStorage.userRefreshToken
                            )
                        } catch (e: Exception) {
                            logger.e("Refreshing access token failed", e)
                            exitProcess(1)
                        }
                    }
                }
            }
        })

        println(
            OrganisationApi(userClient).getUserOrganisations().joinToString("\n", prefix = "\n") {
                "${it.id}) ${it.name}"
            }
        )
        print("Select an organization: ")
        val organizationId = readln().toLong()

        val eventMap = EventApi(userClient).getOrganisationEvents(organizationId).associateBy { it.id }
        println(eventMap.values.joinToString("\n", prefix = "\n") { "${it.id}) ${it.name}" })
        print("Select an Event: ")
        val event = readln().toLong().let { eventMap[it] } ?: error("Selected invalid event")


        val waiterMap = userClient.delegate.get("${App.config.apiBase}v1/config/waiter?organisationId=$organizationId")
            .body<List<Waiter>>()
            .filter { waiter -> waiter.activated && waiter.deleted == null && waiter.events.any { it.id == event.id } }
            .ifEmpty { error("No active Waiter for event ${event.name} found. Create one first.") }
            .associateBy { it.id }

        println(waiterMap.values.joinToString("\n", prefix = "\n") { "${it.id}) ${it.name}" })
        val waiter = readln().toLong().let { waiterMap[it] } ?: error("Selected invalid waiter")

        loginClient.post("${App.config.apiBase}v1/waiter/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(WaiterLogin(waiter.signInToken, "Manual Testing Tool"))
        }.body<TokenDto>().let {
            TmpStorage.waiterAccessToken = it.accessToken
            TmpStorage.waiterRefreshToken = it.refreshToken ?: TmpStorage.userRefreshToken
        }

        val waiterClient = createClient(enableNetworkLogs = true, logger = logger).config {
            install(Auth) {
                bearer {
                    loadTokens {
                        BearerTokens(TmpStorage.waiterAccessToken, TmpStorage.waiterRefreshToken)
                    }

                    refreshTokens {
                        val authApi = AuthApi(createClient(enableNetworkLogs = true, logger = logger))
                        @Suppress("TooGenericExceptionCaught")
                        try {
                            val tokenInfo = authApi.refresh(TmpStorage.waiterRefreshToken)

                            TmpStorage.userAccessToken = tokenInfo.accessToken
                            // Only override when got a new sessionToken
                            tokenInfo.refreshToken?.let { TmpStorage.waiterRefreshToken = it }

                            BearerTokens(
                                accessToken = tokenInfo.accessToken,
                                refreshToken = tokenInfo.refreshToken ?: TmpStorage.waiterRefreshToken
                            )
                        } catch (e: Exception) {
                            logger.e("Refreshing access token failed", e)
                            exitProcess(1)
                        }
                    }
                }
            }
        }

        waiterClient.get("${App.config.apiBase}v1/waiter/myself").bodyAsText()

        logger.i("Successfully logged in with waiter ${waiter.name}")

        // TODO allow adding additional waiters for a more realistic test

        print("Should orders be marked as test orders. Billing will not be tested (y/N): ")
        val markAsTestOrders = when (readln().lowercase()) {
            "y" -> true
            "n", "" -> false
            else -> error("Invalid option")
        }

        println("Type 'start' to start sending random orders. You can stop at any point by pressing 'q'.")
        if (readln().lowercase() != "start") {
            logger.i("Quitting")
            exitProcess(0)
        }

        refreshData(waiterClient, event.id)

        val refreshJob = launch { continuousDataRefresh(waiterClient, event.id) }
        val orderJob = launch(Dispatchers.Default) { placeRandomOrders(waiterClient, markAsTestOrders) }
        val payJob = if (markAsTestOrders) {
            null
        } else {
            launch(Dispatchers.Default) { payRandomTables(waiterClient, event.id) }
        }

        var input: String
        do {
            input = readln().lowercase()
        } while (input != "q" && isActive)

        orderJob.cancelAndJoin()
        payJob?.cancelAndJoin()
        refreshJob.cancelAndJoin()

        // TODO add possibility to pay all leftover orders (when markAsTestOrders == false)
    }
    exitProcess(0)
} catch (e: Exception) {
    logger.e("Error occurred", e)
    exitProcess(1)
}

private suspend fun placeRandomOrders(waiterClient: HttpClient, testOrders: Boolean) = coroutineScope {
    val url = if (testOrders) {
        "${App.config.apiBase}v1/waiter/order/test"
    } else {
        "${App.config.apiBase}v1/waiter/order"
    }
    while (isActive) {
        delay(Random.nextInt(1..30).seconds)
        val tableId = TmpStorage.availableTables.random()

        val orderProducts = buildList {
            repeat(Random.nextInt(1..20)) {
                add(
                    CreateOrder.Product(
                        id = TmpStorage.availableProducts.random(),
                        note = if (Random.nextInt(100) % 10 == 0) randomNote() else null,
                        amount = Random.nextInt(1..50)
                    )
                )
            }
        }

        try {
            waiterClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(CreateOrder(tableId, orderProducts))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.e("Order failed", e)
        }
    }
}

private suspend fun payRandomTables(waiterClient: HttpClient, eventId: ID) = coroutineScope {
    while (isActive) {
        delay(Random.nextInt(1, 30).seconds)
        val payAll = Random.nextInt(100) % 10 == 0 // ~every 10 payment is pay everything on the table

        try {
            val tableId = waiterClient.get("${App.config.apiBase}v1/waiter/table/activeOrders?eventId=$eventId")
                .body<TableIdList>()
                .tableIds
                .random()

            // Ensure that we do not accidentally try to pay the same orderProduct twice
            TmpStorage.tableLocks.getOrPut(tableId) { Mutex() }.withLock {
                val orderProductsToPay = waiterClient.get("${App.config.apiBase}v2/waiter/billing/$tableId")
                    .body<TableBill>()
                    .implodedOrderProducts
                    .flatMap { it.orderProductIds }
                    .shuffled()
                    .let {
                        if (payAll) it else it.take(Random.nextInt(1..it.size))
                    }

                waiterClient.post("${App.config.apiBase}v2/waiter/billing/pay/$tableId") {
                    contentType(ContentType.Application.Json)
                    setBody(PayBill(tableId, orderProductsToPay))
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.e("Payment failed", e)
        }
    }
}

private suspend fun continuousDataRefresh(waiterClient: HttpClient, eventId: ID) = coroutineScope {
    while (isActive) {
        try {
            refreshData(waiterClient, eventId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.e("Refresh failed", e)
        }

        delay(10.seconds)
    }
}

private suspend fun refreshData(waiterClient: HttpClient, eventId: ID) {
    TmpStorage.availableProducts = waiterClient.get("${App.config.apiBase}v1/waiter/product?eventId=$eventId")
        .body<List<ProductGroup>>()
        .flatMap { it.products }
        .filter { !it.soldOut && it.deleted == null }
        .map { it.id }

    TmpStorage.availableTables = waiterClient.get("${App.config.apiBase}v1/waiter/table/group?eventId=$eventId")
        .body<List<TableGroup>>()
        .flatMap { it.tables }
        .map { it.id }
}

private fun randomNote(): String {
    val charset =
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 \\\"'`´#~!?\$€&%()={}\\[\\]_/*+-.,><\\-|°\\^\\\\:;ßäöüÄÖÜ\\n\\r"
    return buildString {
        repeat(Random.nextInt(120)) {
            append(charset[Random.nextInt(charset.length)])
        }
    }
}

private object TmpStorage {
    var userAccessToken = ""
    var userRefreshToken = ""

    var waiterAccessToken = ""
    var waiterRefreshToken = ""

    var availableTables: List<ID> = emptyList()
    var availableProducts: List<ID> = emptyList()

    val tableLocks: ConcurrentMap<ID, Mutex> = ConcurrentHashMap()
}

@Serializable
private data class Waiter(
    val id: ID,
    val name: String,
    val signInToken: String,
    val activated: Boolean,
    val events: List<Event>,
    val deleted: String?
) {
    @Serializable
    data class Event(val id: ID)
}

@Serializable
private data class WaiterLogin(
    val token: String,
    val sessionInformation: String
)

@Serializable
private data class ProductGroup(val products: List<Product>) {
    @Serializable
    data class Product(
        val id: ID,
        val soldOut: Boolean,
        val deleted: String?
    )
}

@Serializable
private data class TableGroup(val tables: List<Table>) {
    @Serializable
    data class Table(val id: ID)
}

@Serializable
private data class TableIdList(val tableIds: List<ID>)

@Serializable
private data class TableBill(val implodedOrderProducts: List<ImplodedOrderProduct>) {
    @Serializable
    data class ImplodedOrderProduct(val orderProductIds: List<ID>)
}

@Serializable
private data class PayBill(
    val tableId: ID,
    val orderProducts: List<ID>
)

@Serializable
private data class CreateOrder(
    val tableId: Long,
    val products: List<Product>
) {
    @Serializable
    data class Product(
        val id: Long,
        val note: String?,
        val amount: Int
    )
}
