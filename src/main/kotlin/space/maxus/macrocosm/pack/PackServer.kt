package space.maxus.macrocosm.pack

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.util.Monitor
import java.io.File
import java.net.InetSocketAddress
import java.nio.file.Files
import java.util.concurrent.ExecutorService

object PackServer {
    private lateinit var server: HttpServer
    private lateinit var networkThread: ExecutorService

    fun hook(file: File) {
        networkThread = Threading.newFixedPool(5)
        server = HttpServer.create(InetSocketAddress(6060), 0)
        server.createContext("/pack", Handler(file))
        server.start()
    }

    private class Handler(val packZip: File) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            Monitor.enter("Pack Exchange for ${exchange.remoteAddress}")
            exchange.sendResponseHeaders(200, packZip.totalSpace)
            val os = exchange.responseBody
            Files.copy(packZip.toPath(), os)
            os.close()
            Monitor.exit()
        }
    }
}
