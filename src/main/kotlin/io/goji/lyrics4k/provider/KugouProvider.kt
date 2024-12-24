package io.goji.lyrics4k.provider

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.goji.lyrics4k.client.LyricsWebClient
import io.goji.lyrics4k.model.MediaData
import org.slf4j.LoggerFactory
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64

class KugouProvider(private val client: LyricsWebClient) : LyricsProvider {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val mapper = jacksonObjectMapper()

    companion object {
        private const val BASE_URL = "http://lyrics.kugou.com/"
        private const val SEARCH_URL = "${BASE_URL}search?ver=1&man=yes&client=pc&keyword=%s&duration=%d"
        private const val LYRIC_URL = "${BASE_URL}download?ver=1&client=pc&id=%s&accesskey=%s&fmt=lrc&charset=utf8"
    }

    override suspend fun getLyrics(data: MediaData): String {
        val lyricUrl = getLyricUrl(data) ?: return ""
        return try {
            val response = client.get(lyricUrl)
            val jsonResponse: Map<String, Any> = mapper.readValue(response)
            val encodedLyric = jsonResponse["content"] as String
            String(Base64.getDecoder().decode(encodedLyric))
        } catch (e: Exception) {
            logger.error("Error getting lyrics: ${e.message}")
            ""
        }
    }

    private suspend fun getLyricUrl(data: MediaData): String? {
        val searchUrl = SEARCH_URL.format(URLEncoder.encode(getSearchKey(data), StandardCharsets.UTF_8), data.duration)
        return try {
            val response = client.get(searchUrl)
            val jsonResponse: Map<String, Any> = mapper.readValue(response)

            if (jsonResponse["status"] as Double != 200.0) return null

            val candidates = jsonResponse["candidates"] as List<Map<String, Any>>
            candidates.maxByOrNull { it["score"] as Double }?.let {
                LYRIC_URL.format(it["id"], it["accesskey"])
            }
        } catch (e: Exception) {
            logger.error("Error getting lyric URL: ${e.message}")
            null
        }
    }

    private fun getSearchKey(data: MediaData): String = when {
        data.artist.isNotEmpty() -> "${data.artist}-${data.title}"
        data.album.isNotEmpty() -> "${data.album}-${data.title}"
        else -> data.title
    }
}
