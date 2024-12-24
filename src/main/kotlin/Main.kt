package io.goji

import io.goji.lyrics4k.client.LyricsWebClient
import io.goji.lyrics4k.model.MediaData
import io.goji.lyrics4k.provider.KugouProvider
import io.goji.lyrics4k.provider.NeteaseProvider
import io.goji.lyrics4k.provider.QMusicProvider
import io.goji.lyrics4k.service.LyricsService
import io.vertx.core.Vertx
import kotlinx.coroutines.runBlocking

fun main() {
    val vertx = Vertx.vertx()
    val webClient = LyricsWebClient(vertx)

    val lyricsService = LyricsService().apply {
        addProvider(KugouProvider(webClient))
        addProvider(NeteaseProvider(webClient))
        addProvider(QMusicProvider(webClient))
    }

    // Example usage with coroutines
    runBlocking {
        val mediaData = MediaData(
            title = "冠菊",
            artist = "初星学园",
        )

        try {
            val lyrics = lyricsService.getLyrics(mediaData)
            println("Found lyrics: $lyrics")
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }

    vertx.close()
}
