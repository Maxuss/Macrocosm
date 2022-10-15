package space.maxus.macrocosm.util.http

import space.maxus.macrocosm.Macrocosm
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object Requests {
    private fun openConnection(url: String): HttpsURLConnection {
        val conn = URL(url).openConnection()
        conn.setRequestProperty("User-Agent", "Macrocosm/${Macrocosm.version}")
        return conn as HttpsURLConnection
    }

    fun get(link: String): ByteArray {
        val conn = openConnection(link)
        return conn.inputStream.readAllBytes()
    }
}
