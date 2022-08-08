package space.maxus.macrocosm.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import space.maxus.macrocosm.pack.PackProvider
import space.maxus.macrocosm.util.GSON

@Suppress("DeferredResultUnused")
suspend fun spinApi() {
    coroutineScope {
        async {
            module()
        }
    }
}

suspend fun module() {
    embeddedServer(Netty, port = 6060) {
        routing {
            get("/pack") {
                call.respondFile(PackProvider.packZip)
            }
        }
    }.start(true)
}

private suspend fun ApplicationCall.respondJson(obj: Any?, code: HttpStatusCode = HttpStatusCode.OK) {
    this.respondText(if(obj == null) "{\"error\": \"Internal Server Error\"}" else GSON.toJson(obj), ContentType.Application.Json, if(obj == null) HttpStatusCode.InternalServerError else code)
}
