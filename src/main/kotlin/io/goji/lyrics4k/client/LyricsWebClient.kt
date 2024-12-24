package io.goji.lyrics4k.client

import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.kotlin.coroutines.coAwait
import org.slf4j.LoggerFactory

class LyricsWebClient(vertx: Vertx) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val client: WebClient

    init {
        val options = WebClientOptions()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
            .setConnectTimeout(2000)
        client = WebClient.create(vertx, options)
    }

    suspend fun get(url: String, referer: String? = null): String {
        return try {
            val request = client.getAbs(url)
            referer?.let { request.putHeader("Referer", it) }

            val response = request.send().coAwait()
            if (response.statusCode() == 200) {
                response.bodyAsString()
            } else {
                throw Exception("HTTP ${response.statusCode()}")
            }
        } catch (e: Exception) {
            logger.error("Error fetching $url: ${e.message}")
            throw e
        }
    }
}
