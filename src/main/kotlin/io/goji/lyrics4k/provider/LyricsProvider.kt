package io.goji.lyrics4k.provider

import io.goji.lyrics4k.model.MediaData

interface LyricsProvider {
    suspend fun getLyrics(data: MediaData): String
}
