package io.goji.lyrics4k.service

import io.goji.lyrics4k.model.MediaData
import io.goji.lyrics4k.provider.LyricsProvider
import org.slf4j.LoggerFactory

class LyricsService {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val providers = mutableListOf<LyricsProvider>()

    suspend fun getLyrics(data: MediaData): String {
        if (providers.isEmpty()) {
            throw IllegalStateException("No providers configured")
        }

        for (provider in providers) {
            val lyrics = provider.getLyrics(data)
            if (lyrics.length > 5) {
                return lyrics
            }
        }

        throw NoSuchElementException("Lyrics not found")
    }

    fun addProvider(provider: LyricsProvider) {
        providers.add(provider)
    }
}
