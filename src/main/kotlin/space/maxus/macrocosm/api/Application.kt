package space.maxus.macrocosm.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import io.papermc.paper.adventure.PaperAdventure
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import org.bukkit.Bukkit
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.MacrocosmConstants
import space.maxus.macrocosm.api.KeyManager.validateKey
import space.maxus.macrocosm.bazaar.Bazaar
import space.maxus.macrocosm.bazaar.BazaarElement
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.util.GSON
import space.maxus.macrocosm.util.data.ExpiringContainer
import space.maxus.macrocosm.util.data.MutableContainer
import space.maxus.macrocosm.util.general.SuspendConditionalCallback
import space.maxus.macrocosm.util.general.getId
import space.maxus.macrocosm.util.general.putId
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.Duration
import java.util.*

/**
 * Spins up the macrocosm Rest API, basically wrapping [serverSpin] in coroutine scope
 */
suspend fun spinApi() =
    coroutineScope {
        async {
            serverSpin()
        }
    }

private val offlineInventoryCompoundCache = MutableContainer.empty<String>()
private val onlineInventoryCompoundCache = ExpiringContainer.empty<String>(Duration.ofMinutes(5).toMillis())

/**
 * The main configuration of the Macrocosm API, to find more detailed endpoint information view the swagger spec
 */
fun Application.module() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondFailure("Internal Server Error: ${cause.message}", HttpStatusCode.InternalServerError)
        }

        status(HttpStatusCode.NotFound) { call, status ->
            call.respondFailure("Endpoint ${call.request.uri} not found", status)
        }
    }

    routing {
        route("/doc") {
            get {
                call.respondMacrocosmResource("index.html", "doc")
            }

            get("{static...}") {
                val joined = call.parameters.getAll("static")?.joinToString(File.separator) ?: return@get
                call.respondMacrocosmResource(joined, "doc")
            }
        }

        route("/resx") {
            get("/webhook_icon.png") {
                call.respondMacrocosmResource("pack.png", "pack")
            }
        }

        // upgraded api version to V2
        route("v2") {

            // status
            get("/status") {
                call.respondSuccess(
                    Status(
                        "OPERATING",
                        Macrocosm.isInDevEnvironment,
                        MacrocosmConstants.API_VERSION.toString(),
                        MacrocosmConstants.VERSION.toString()
                    )
                )
            }

            // resources
            route("/resources") {
                get {
                    call.respondSuccess(
                        AvailableRegistries(
                            Registry.iter().filter { reg -> reg.value.shouldBeExposed }.keys
                        )
                    )
                }

                route("{registry}") {
                    get {
                        val reg = Registry.findOrNull(
                            Identifier.parse(
                                call.parameters["registry"] ?: return@get call.respondFailure(
                                    "Registry not specified!",
                                    HttpStatusCode.BadRequest
                                )
                            )
                        ) ?: return@get call.respondFailure(
                            "Could not find registry '${call.parameters["registry"]}'!",
                            HttpStatusCode.NotFound
                        )
                        if (!reg.shouldBeExposed)
                            return@get call.respondFailure("This registry is hidden!", HttpStatusCode.BadRequest)
                        call.respondSuccess(GetRegistry(reg.iter()))
                    }
                    get("{element}") {
                        val regParam = call.parameters["registry"] ?: return@get call.respondFailure(
                            "Registry not specified!",
                            HttpStatusCode.BadRequest
                        )
                        val reg =
                            Registry.findOrNull(Identifier.parse(regParam)) ?: return@get call.respondFailure(
                                "Could not find registry '${regParam}'!",
                                HttpStatusCode.NotFound
                            )
                        if (!reg.shouldBeExposed)
                            return@get call.respondFailure("This registry is hidden!", HttpStatusCode.BadRequest)
                        val element = call.parameters["element"] ?: return@get call.respondFailure(
                            "No key provided!",
                            HttpStatusCode.BadRequest
                        )
                        val item = reg.findOrNull(Identifier.parse(element)) ?: return@get call.respondFailure(
                            "Could not find element $element in '${regParam}' registry!",
                            HttpStatusCode.NotFound
                        )
                        call.respondSuccess(GetRegistryElement(item))
                    }
                }
            }

            // players
            route("/players") {
                getWithKey(APIPermission.VIEW_PLAYER_DATA) {
                    call.respondJson(OnlinePlayers(Bukkit.getOnlinePlayers().associate { p -> p.name to p.uniqueId }))
                }

                getWithKey("/{player}", APIPermission.VIEW_PLAYER_DATA) {
                    val player = parseMacrocosmPlayer() ?: return@getWithKey
                    call.respondSuccess(
                        GeneralPlayerData(
                            player.rank,
                            player.firstJoin,
                            player.lastJoin,
                            player.playtime
                        )
                    )
                }

                getWithKey("/{player}/status", APIPermission.VIEW_PLAYER_DATA) {
                    val player = parseMacrocosmPlayer() ?: return@getWithKey
                    call.respondSuccess(PlayerStatus(true, player.ref, player.paper != null))
                }

                getWithKey("/{player}/inventory", APIPermission.VIEW_PLAYER_DATA) {
                    val player = parseMacrocosmPlayer() ?: return@getWithKey

                    var inventoryData = "null"
                    val online = player.paper
                    if (online == null) {
                        offlineInventoryCompoundCache.take(player.ref) {
                            inventoryData = it
                        }.otherwise {
                            val dataCompound =
                                MinecraftServer.getServer().playerDataStorage.getPlayerData(player.ref.toString())
                            val inventoryTag = dataCompound.getList("Inventory", CompoundTag.TAG_COMPOUND.toInt())
                            inventoryData = cacheInventory(player.ref, inventoryTag)
                        }.call()
                    } else {
                        onlineInventoryCompoundCache.trySetExpiring(player.ref) {
                            val dataCompound =
                                MinecraftServer.getServer().playerDataStorage.getPlayerData(player.ref.toString())
                            val inventoryTag = dataCompound.getList("Inventory", CompoundTag.TAG_COMPOUND.toInt())
                            inventoryData = cacheInventory(player.ref, inventoryTag)
                            inventoryData
                        }.otherwise {
                            onlineInventoryCompoundCache.take(player.ref) {
                                inventoryData = it
                            }.otherwise {
                                val dataCompound =
                                    MinecraftServer.getServer().playerDataStorage.getPlayerData(player.ref.toString())
                                val inventoryTag = dataCompound.getList("Inventory", CompoundTag.TAG_COMPOUND.toInt())
                                inventoryData = cacheInventory(player.ref, inventoryTag)
                                onlineInventoryCompoundCache[player.ref] = inventoryData
                            }.call()
                        }.call()
                    }
                    if (inventoryData != "null") {
                        call.respondSuccess(PlayerInventory(inventoryData))
                    } else {
                        call.respondFailure(inventoryData)
                    }
                }

                getWithKey("/{player}/balance", APIPermission.VIEW_PLAYER_DATA) {
                    val player = parseMacrocosmPlayer() ?: return@getWithKey
                    call.respondSuccess(PlayerBalance(player.bank, player.purse))
                }

                getWithKey("/{player}/skills", APIPermission.VIEW_PLAYER_DATA) {
                    val player = parseMacrocosmPlayer() ?: return@getWithKey
                    call.respondSuccess(PlayerSkills(player.skills.skillExp, player.collections.colls))
                }
            }

            route("/bazaar") {
                getWithKey(APIPermission.VIEW_BAZAAR_DATA) {
                    call.respondSuccess(BazaarStatus(Bazaar.table.entries, Bazaar.table.ordersTotal))
                }

                getWithKey("/items", APIPermission.VIEW_BAZAAR_DATA) {
                    call.respondSuccess(BazaarElements(BazaarElement.allKeys))
                }

                getWithKey("/orders/{item}", APIPermission.VIEW_BAZAAR_DATA) {
                    val itemParam = call.parameters["item"] ?: return@getWithKey call.respondFailure(
                        "Item not provided!",
                        HttpStatusCode.BadRequest
                    )
                    val data = Bazaar.table.itemData
                    val id = Identifier.parse(itemParam)
                    if (!data.containsKey(id))
                        return@getWithKey call.respondFailure(
                            "Item $id was not found in bazaar storage!",
                            HttpStatusCode.NotFound
                        )
                    val itemData = data[id]!!
                    call.respondSuccess(ItemOrders(itemData.buy.toList().take(100), itemData.sell.toList().take(100)))
                }

                getWithKey("/summary/{item}", APIPermission.VIEW_BAZAAR_DATA) {
                    val itemParam = call.parameters["item"] ?: return@getWithKey call.respondJson(
                        "Item not provided!",
                        HttpStatusCode.BadRequest
                    )
                    val data = Bazaar.table.itemData
                    val id = Identifier.parse(itemParam)
                    if (!data.containsKey(id))
                        return@getWithKey call.respondFailure(
                            "Item $id was not found in bazaar storage!",
                            HttpStatusCode.NotFound
                        )
                    call.respondSuccess(Bazaar.table.summary(id))
                }
            }
        }
    }
}

/**
 * Builds up the embedded macrocosm Rest API server using the Netty ktor backend
 */
fun serverSpin() {
    embeddedServer(Netty, applicationEngineEnvironment {
        log = Macrocosm.slF4JLogger

        connector {
            port = 4343
            host = "127.0.0.1"
        }

        module(Application::module)
    }).start(true)
}

private fun cacheInventory(player: UUID, inventory: ListTag): String {
    val resultList = ListTag()
    inventory.forEach { itemTag ->
        val cmp = itemTag as CompoundTag
        val resultCompound = CompoundTag()
        resultCompound.putId("ItemID", cmp.getId("id"))
        resultCompound.putInt("Amount", cmp.getByte("Count").toInt())
        if (cmp.contains("tag")) {
            val tagCompound = cmp.getCompound("tag")
            resultCompound.put("MacrocosmValues", tagCompound.getCompound("MacrocosmValues"))

            val displayCmp = tagCompound.getCompound("display")

            val nameStr = displayCmp.getString("Name")
            resultCompound.putString(
                "Name",
                PaperAdventure.asAdventure(Component.Serializer.fromJson(nameStr)).str()
            )
            val loreList = ListTag()
            displayCmp.getList("Lore", StringTag.TAG_STRING.toInt()).forEach { ele ->
                val str = ele.asString
                val component = PaperAdventure.asAdventure(Component.Serializer.fromJson(str))
                if (component != net.kyori.adventure.text.Component.empty())
                    loreList.add(StringTag.valueOf(component.str()))
            }
            resultCompound.put("Lore", loreList)
        }
        resultList.add(resultCompound)
    }
    val out = ByteArrayOutputStream()
    val outCmp = CompoundTag()
    outCmp.put("Inventory", resultList)
    NbtIo.writeCompressed(outCmp, out)
    val encodedBytes = Base64.getEncoder().encode(out.toByteArray())
    val str = String(encodedBytes, Charsets.UTF_8)
    offlineInventoryCompoundCache[player] = str
    return str
}

private fun tryRetrievePlayer(param: String): MacrocosmPlayer? {
    val online = try {
        val id = UUID.fromString(param)
        if (Macrocosm.loadedPlayers.containsKey(id))
            return Macrocosm.loadedPlayers[id]
        else Bukkit.getPlayer(id)
    } catch (e: IllegalArgumentException) {
        Bukkit.getPlayer(param)
    } ?: run {
        val offline = try {
            Bukkit.getOfflinePlayer(UUID.fromString(param))
        } catch (e: IllegalArgumentException) {
            Bukkit.getOfflinePlayer(param)
        }
        if (Macrocosm.loadedPlayers.containsKey(offline.uniqueId))
            return Macrocosm.loadedPlayers[offline.uniqueId]
        if (!Macrocosm.playersLazy.contains(offline.uniqueId)) {
            return null
        }
        val loaded = MacrocosmPlayer.loadPlayer(offline.uniqueId) ?: return null
        Macrocosm.loadedPlayers[offline.uniqueId] = loaded
        return loaded
    }
    return Macrocosm.loadedPlayers[online.uniqueId]
}

private suspend fun PipelineContext<Unit, ApplicationCall>.parseMacrocosmPlayer(): MacrocosmPlayer? {
    val playerParam = call.parameters["player"] ?: return call.respondFailure(
        "Player parameter not provided!",
        HttpStatusCode.BadRequest
    ).let { null }
    return tryRetrievePlayer(playerParam) ?: return call.respondFailure(
        "Player '${playerParam}' not found!",
        HttpStatusCode.NotFound
    ).let { null }
}

private suspend fun ApplicationCall.respondSuccess(obj: Any?, code: HttpStatusCode = HttpStatusCode.OK) {
    this.respondJson(Success(true, obj), code)
}

private suspend fun ApplicationCall.respondFailure(
    obj: Any,
    code: HttpStatusCode = HttpStatusCode.InternalServerError
) {
    this.respondJson(Failure(false, obj), code)
}

private suspend fun ApplicationCall.respondJson(obj: Any?, code: HttpStatusCode = HttpStatusCode.OK) {
    this.respondText(
        if (obj == null) "{\"success\":false, \"error\": \"Internal Server Error\"}" else GSON.toJson(obj),
        ContentType.Application.Json,
        if (obj == null) HttpStatusCode.InternalServerError else code
    )
}

private suspend fun ApplicationCall.respondMacrocosmResource(path: String, base: String) {
    val res = resolveResource(path, classLoader = Macrocosm.javaClass.classLoader, resourcePackage = base)
    if (res != null) {
        respond(res)
    }
}

@KtorDsl
private fun Route.getWithKey(perm: APIPermission, body: PipelineInterceptor<Unit, ApplicationCall>) {
    method(HttpMethod.Get) {
        handle {
            call.requireKey(perm).then {
                body(it)
            }.call()
        }
    }
}

@KtorDsl
private fun Route.getWithKey(path: String, perm: APIPermission, body: PipelineInterceptor<Unit, ApplicationCall>) {
    route(path, HttpMethod.Get) {
        handle {
            call.requireKey(perm).then {
                body(it)
            }.call()
        }
    }
}

private suspend fun ApplicationCall.requireKey(perm: APIPermission): SuspendConditionalCallback {
    when (val result = validateKey(perm)) {
        KeyManager.ValidationResult.SUCCESS -> return SuspendConditionalCallback.suspendSuccess()
        KeyManager.ValidationResult.NO_KEY_PROVIDED -> {
            respondFailure("${result.name}: Provide API key for this endpoint", HttpStatusCode.Forbidden)
            return SuspendConditionalCallback.suspendFail()
        }

        KeyManager.ValidationResult.INVALID_KEY -> {
            respondFailure("${result.name}: The API key you provided was invalid", HttpStatusCode.Forbidden)
            return SuspendConditionalCallback.suspendFail()
        }

        KeyManager.ValidationResult.KEY_THROTTLE -> {
            respondFailure(
                "${result.name}: API key throttle, max amount of requests reached (100)",
                HttpStatusCode.Forbidden
            )
            return SuspendConditionalCallback.suspendFail()
        }

        KeyManager.ValidationResult.INSUFFICIENT_PERMISSIONS -> {
            respondFailure(
                "${result.name}: This endpoint requires your key to have ${perm.name} permission",
                HttpStatusCode.Unauthorized
            )
            return SuspendConditionalCallback.suspendFail()
        }

        KeyManager.ValidationResult.INVALID_FORMAT -> {
            respondFailure("${result.name}: Invalid key format used (probably legacy)", HttpStatusCode.Forbidden)
            return SuspendConditionalCallback.suspendFail()
        }
    }
}
