package space.maxus.macrocosm.http

import java.net.HttpURLConnection
import java.net.URL

object Requests {
    fun get(url: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        return String(conn.inputStream.readAllBytes())
    }
}
