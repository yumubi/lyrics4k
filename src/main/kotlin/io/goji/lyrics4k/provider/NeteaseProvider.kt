package io.goji.lyrics4k.provider

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.goji.lyrics4k.client.LyricsWebClient
import io.goji.lyrics4k.model.MediaData
import org.slf4j.LoggerFactory
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class NeteaseProvider(private val client: LyricsWebClient) : LyricsProvider {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val mapper = jacksonObjectMapper()

    companion object {
        private const val BASE_URL = "http://music.163.com/api/"
        private const val SEARCH_URL = "${BASE_URL}search/get?s=%s&type=1&offset=0&limit=5"
        private const val LYRIC_URL = "${BASE_URL}song/lyric?os=pc&id=%d&lv=-1&kv=-1&tv=-1"
    }

    override suspend fun getLyrics(data: MediaData): String {
        val lyricUrl = getLyricUrl(data) ?: return ""
        return try {
            val response = client.get(lyricUrl)
            val jsonResponse: Map<String, Any> = mapper.readValue(response)
            (jsonResponse["lrc"] as Map<String, Any>)["lyric"] as String
        } catch (e: Exception) {
            logger.error("Error getting lyrics: ${e.message}")
            ""
        }
    }

    private suspend fun getLyricUrl(data: MediaData): String? {
        val searchUrl = SEARCH_URL.format(URLEncoder.encode(getSearchKey(data), StandardCharsets.UTF_8))
        return try {
            val response = client.get(searchUrl)
            val jsonResponse: Map<String, Any> = mapper.readValue(response)

            if (jsonResponse["code"] as Double != 200.0) return null

            val songs = (jsonResponse["result"] as Map<String, Any>)["songs"] as List<Map<String, Any>>
            songs.find { it["name"] as String == data.title }?.let {
                LYRIC_URL.format((it["id"] as Number).toInt())
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
