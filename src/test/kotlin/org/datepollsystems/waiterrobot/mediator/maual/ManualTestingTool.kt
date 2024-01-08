package org.datepollsystems.waiterrobot.mediator.maual

import co.touchlab.kermit.*
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
import org.datepollsystems.waiterrobot.mediator.data.api.AuthApi
import org.datepollsystems.waiterrobot.mediator.data.api.EventApi
import org.datepollsystems.waiterrobot.mediator.data.api.OrganisationApi
import org.datepollsystems.waiterrobot.mediator.data.api.dto.TokenDto
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.seconds

private val fileLogger = Logger(
    loggerConfigInit(object : LogWriter() {
        private val fileChangeLock = Semaphore(1)
        private var fileCount = 1
        private var lineCounter = AtomicInteger()
        private val startEpoch = Date().toInstant().epochSecond
        private var logFile = File("ManualTestingTool-$startEpoch-0.log")

        init {
            println("You will find the logs here: ${logFile.absolutePath}")
        }

        override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
            val str = DefaultFormatter.formatMessage(severity, Tag(tag), Message(message)) +
                (throwable?.stackTraceToString()?.let { "\n$it" } ?: "") + "\n"
            logFile.appendText(str)
            if (severity == Severity.Error) {
                System.err.print(str)
            }

            // Rotate the log file, so that we do not get one huge file that can't be opened
            if (lineCounter.addAndGet(str.lines().size) >= 100) {
                if (fileChangeLock.tryAcquire()) {
                    try {
                        logFile = File("ManualTestingTool-$startEpoch-$fileCount.log")
                        fileCount++
                        lineCounter.set(0)
                    } finally {
                        fileChangeLock.release()
                    }
                }
            }
        }
    })
)

suspend fun main(): Unit = try {
    coroutineScope {
        startKoin {
            modules(
                module {
                    factory { (tag: String?) -> if (tag != null) fileLogger.withTag(tag) else fileLogger }
                }
            )
        }

        val loginClient = createClient(enableNetworkLogs = true, logger = fileLogger)

        var email = prompt("Email/Username")
        val password = System.console()?.readPassword("Password: ")?.joinToString("") ?: run {
            prompt("Password")
        }

        App.config = Config.getFromLoginIdentifier(email)
        email = email.removeLoginIdentifierEnvPrefix()
        if (App.config == Config.Prod) {
            error("Testing on Prod is not possible. Use lava://EMAIL for login to lava (or local://EMAIL)")
        }

        AuthApi(loginClient).login(email, password).let {
            TmpStorage.userAccessToken = it.accessToken
            TmpStorage.userRefreshToken = it.refreshToken ?: TmpStorage.userRefreshToken
        }

        val userClient = AuthorizedClient(
            createClient(enableNetworkLogs = true, logger = fileLogger).config {
                install(Auth) {
                    bearer {
                        loadTokens {
                            BearerTokens(TmpStorage.userAccessToken, TmpStorage.userRefreshToken)
                        }

                        refreshTokens {
                            val authApi = AuthApi(createClient(enableNetworkLogs = true, logger = fileLogger))
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
                                fileLogger.e("Refreshing access token failed", e)
                                exitProcess(1)
                            }
                        }
                    }
                }
            }
        )

        printlnLogged(
            OrganisationApi(userClient).getUserOrganisations().joinToString("\n", prefix = "\n") {
                "${it.id}) ${it.name}"
            }
        )

        val organizationId = prompt("Select an organization").toLong()

        val eventMap = EventApi(userClient).getOrganisationEvents(organizationId).associateBy { it.id }
        printlnLogged(eventMap.values.joinToString("\n", prefix = "\n") { "${it.id}) ${it.name}" })
        val event = prompt("Select an Event").toLong().let { eventMap[it] } ?: error("Selected invalid event")

        val waiterMap = userClient.delegate.get("${App.config.apiBase}v1/config/waiter?organisationId=$organizationId")
            .body<List<Waiter>>()
            .filter { waiter -> waiter.activated && waiter.deleted == null && waiter.events.any { it.id == event.id } }
            .ifEmpty { error("No active Waiter for event ${event.name} found. Create one first.") }
            .associateBy { it.id }

        var addAnotherWaiter: String
        val waiterClients = buildMap {
            do {
                try {
                    val availableWaiters = waiterMap.filter { it.key !in this.keys }
                    printlnLogged(availableWaiters.values.joinToString("\n", prefix = "\n") { "${it.id}) ${it.name}" })
                    val waiter = prompt("Select a Waiter").toLong().let { availableWaiters[it] }
                        ?: error("Selected invalid waiter")

                    loginClient.post("${App.config.apiBase}v1/waiter/auth/login") {
                        contentType(ContentType.Application.Json)
                        setBody(WaiterLogin(waiter.signInToken, "Manual Testing Tool"))
                    }.body<TokenDto>().let {
                        TmpStorage.waiterAccessToken = it.accessToken
                        TmpStorage.waiterRefreshToken = it.refreshToken ?: TmpStorage.userRefreshToken
                    }

                    val waiterClient = WaiterClient(
                        createClient(enableNetworkLogs = true, logger = fileLogger).config {
                            install(Auth) {
                                bearer {
                                    loadTokens {
                                        BearerTokens(TmpStorage.waiterAccessToken, TmpStorage.waiterRefreshToken)
                                    }

                                    refreshTokens {
                                        val authApi =
                                            AuthApi(createClient(enableNetworkLogs = true, logger = fileLogger))
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
                                            fileLogger.e("Refreshing access token failed", e)
                                            exitProcess(1)
                                        }
                                    }
                                }
                            }
                        }
                    )

                    waiterClient.get("${App.config.apiBase}v1/waiter/myself").bodyAsText()
                    put(waiter.id, waiterClient)
                    fileLogger.i("Successfully logged in with waiter ${waiter.name}")
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    fileLogger.e("Waiter login failed", e)
                }

                addAnotherWaiter = prompt("Do you want to register another Waiter (y/N)")
                if (addAnotherWaiter != "y" && isEmpty()) {
                    fileLogger.i("At least one Waiter required.")
                    printlnLogged("At least one Waiter required.")
                }
            } while (addAnotherWaiter == "y" && isActive)
        }.values

        val markAsTestOrders =
            when (prompt("Should orders be marked as test orders. Billing will not be tested (y/N)")) {
                "y" -> true
                "n", "" -> false
                else -> error("Invalid option")
            }


        if (prompt(
                "Type 'start' to start sending random orders. You can stop at any point by pressing 'q'.\n" +
                    "Make sure to also start a Mediator (with virtual printer), " +
                    "otherwise the next Mediator start on that event will have to print a ton."
            ) != "start"
        ) {
            fileLogger.i("Quitting")
            exitProcess(0)
        }

        refreshData(waiterClients.first(), event.id)

        val refreshJobs = mutableListOf<Job>()
        val orderJobs = mutableListOf<Job>()
        val payJobs = mutableListOf<Job>()
        waiterClients.forEach { waiterClient ->
            refreshJobs.add(
                launch { continuousDataRefresh(waiterClient, event.id) }
            )
            orderJobs.add(
                launch(Dispatchers.Default) { placeRandomOrders(waiterClient, markAsTestOrders) }
            )
            if (!markAsTestOrders) {
                payJobs.add(
                    launch(Dispatchers.Default) { payRandomTables(waiterClient, event.id) }
                )
            }
        }

        var input: String
        do {
            input = prompt("Press 'q' and enter to stop")
        } while (input != "q" && isActive)

        printlnLogged("Shutting down")
        orderJobs.forEach { it.cancelAndJoin() }
        payJobs.forEach { it.cancelAndJoin() }
        refreshJobs.forEach { it.cancelAndJoin() }

        if (!markAsTestOrders) {
            if (prompt("Do you want to mark all leftover orders as payed (Y/n)") == "y") {
                printlnLogged("Marking all orders as payed...")
                waiterClients.first().getTableIdsWithOpenOrders(event.id).forEach {
                    waiterClients.first().payTable(it, payAll = true)
                    printlnLogged("Payed all orders of table $it")
                }
                printlnLogged("Marked all orders as payed.")
            }
        }
    }
    exitProcess(0)
} catch (e: Exception) {
    fileLogger.e("Error occurred", e)
    exitProcess(1)
}

private suspend fun placeRandomOrders(waiterClient: WaiterClient, testOrders: Boolean) = coroutineScope {
    val url = if (testOrders) {
        "${App.config.apiBase}v1/waiter/order/test"
    } else {
        "${App.config.apiBase}v1/waiter/order"
    }
    while (isActive) {
        delay(Random.nextInt(1..30).seconds)
        val tableId = TmpStorage.availableTables.randomOrNull() ?: run {
            fileLogger.e("No tables available. Quitting...")
            exitProcess(3)
        }

        val orderProducts = buildList {
            repeat(Random.nextInt(1..20)) {
                add(
                    CreateOrder.Product(
                        id = TmpStorage.availableProducts.randomOrNull() ?: run {
                            fileLogger.e("No more products available. Quitting...")
                            exitProcess(2)
                        },
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
            if (e.message?.contains(Regex("too small stock|sold out")) == true) {
                // Ignore it's expected
                fileLogger.w("Too small stock: ${e.message}")
                continue
            }
            fileLogger.e("Order failed", e)
        }
    }
}

private suspend fun payRandomTables(waiterClient: WaiterClient, eventId: ID) = coroutineScope {
    while (isActive) {
        delay(Random.nextInt(1, 30).seconds)
        val payAll = Random.nextInt(100) % 10 == 0 // ~every 10 payment is pay everything on the table

        try {
            val tableId = waiterClient.getTableIdsWithOpenOrders(eventId).randomOrNull() ?: continue
            waiterClient.payTable(tableId, payAll)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            fileLogger.e("Payment failed", e)
        }
    }
}

private suspend fun continuousDataRefresh(waiterClient: WaiterClient, eventId: ID) = coroutineScope {
    while (isActive) {
        try {
            refreshData(waiterClient, eventId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            fileLogger.e("Refresh failed", e)
        }

        delay(10.seconds)
    }
}

private suspend fun refreshData(waiterClient: WaiterClient, eventId: ID) {
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
        repeat(Random.nextInt(1..120)) {
            append(charset[Random.nextInt(charset.length)])
        }
    }
}

private suspend fun WaiterClient.getTableIdsWithOpenOrders(eventId: ID): Set<ID> {
    return try {
        get("${App.config.apiBase}v1/waiter/table/activeOrders?eventId=$eventId")
            .body<TableIdList>()
            .tableIds
            .toSet()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        fileLogger.e("Could not get tables with open orders", e)
        emptySet()
    }
}

private suspend fun WaiterClient.payTable(tableId: ID, payAll: Boolean) {
    try {
        // Ensure that we do not accidentally try to pay the same orderProduct twice
        TmpStorage.tableLocks.getOrPut(tableId) { Mutex() }.withLock {
            val orderProductsToPay = get("${App.config.apiBase}v2/waiter/billing/$tableId")
                .body<TableBill>()
                .implodedOrderProducts
                .flatMap { it.orderProductIds }
                .shuffled()
                .let {
                    if (payAll) it else it.take(Random.nextInt(1..it.size))
                }

            post("${App.config.apiBase}v2/waiter/billing/pay/$tableId") {
                contentType(ContentType.Application.Json)
                setBody(PayBill(tableId, orderProductsToPay))
            }
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        fileLogger.e("Payment failed", e)
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

private class WaiterClient(private val delegate: HttpClient) {
    suspend fun get(url: String, block: HttpRequestBuilder.() -> Unit = {}) = delegate.get(url, block)
    suspend fun post(url: String, block: HttpRequestBuilder.() -> Unit = {}) = delegate.post(url, block)
}

private fun printlnLogged(text: String) {
    fileLogger.withTag("Console log").i(text)
    println(text)
}

private fun prompt(prompt: String): String {
    fileLogger.withTag("Console prompt").i(prompt)
    print("$prompt: ")
    return readln().also { fileLogger.withTag("Console entered").i(it) }.lowercase()
}
