package space.maxus.macrocosm.pack

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import space.maxus.macrocosm.Macrocosm
import java.io.File

// Code partially taken from Nova licensed under LGPL:
// https://github.com/xenondevs/Nova/blob/6822d616d6e5b441dad5b9baeb79d3de6ac40689/nova/src/main/kotlin/xyz/xenondevs/nova/data/resources/upload/service/CustomMultiPart.kt
object PackUploader {
    private const val url: String = "https://resourcepack.host/index.php"
    private val urlRegex: Regex = Regex("(http://resourcepack\\.host/dl/[^\"]+)")
    private val httpClient: HttpClient = HttpClient(Java) {
        expectSuccess = false
    }

    suspend fun uploadPack(pack: File): String {
        Macrocosm.logger.info("Uploading pack to resourcepack.host...")

        val response = httpClient.submitFormWithBinaryData(url, formData {
            append("pack", pack.readBytes(), Headers.build {
                append(HttpHeaders.ContentType, ContentType.Application.Zip)
                append(HttpHeaders.ContentDisposition, "filename=\"${pack.name}\"")
            })
        })
        val responseString = response.body<String>()
        require(response.status == HttpStatusCode.OK) { "Upload failed with status ${response.status} and response $responseString" }
        val parsed = urlRegex.find(responseString)?.groupValues?.getOrNull(1)
            ?: throw IllegalArgumentException("No url found in response: $responseString")
        Macrocosm.logger.info("Finished uploading to resourcepack.host! URL is $parsed!")
        return parsed
    }
}
