package io.goji.lyrics4k.provider

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.goji.lyrics4k.client.LyricsWebClient
import io.goji.lyrics4k.model.MediaData
import org.slf4j.LoggerFactory
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

class QMusicProvider(private val client: LyricsWebClient) : LyricsProvider {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val mapper = jacksonObjectMapper()

    companion object {
        private const val BASE_URL = "https://c.y.qq.com/"
        private const val REFERER_URL = "https://y.qq.com"
        private const val SEARCH_URL = "${BASE_URL}soso/fcgi-bin/client_search_cp?w=%s&format=json"
        private const val LYRIC_URL = "${BASE_URL}lyric/fcgi-bin/fcg_query_lyric_yqq.fcg?songmid=%s&format=json"
    }

    override suspend fun getLyrics(data: MediaData): String {
        val lyricUrl = getLyricUrl(data) ?: return ""
        return try {
            val response = client.get(lyricUrl, REFERER_URL)
            val jsonResponse: Map<String, Any> = mapper.readValue(response)
            val encodedLyric = jsonResponse["lyric"] as String
            String(Base64.getDecoder().decode(encodedLyric))
        } catch (e: Exception) {
            logger.error("Error getting lyrics: ${e.message}")
            ""
        }
    }

    private suspend fun getLyricUrl(data: MediaData): String? {
        val searchUrl = SEARCH_URL.format(URLEncoder.encode(getSearchKey(data), StandardCharsets.UTF_8))
        return try {
            val response = client.get(searchUrl, REFERER_URL)
            val jsonResponse: Map<String, Any> = mapper.readValue(response)

            if (jsonResponse["code"] as Number != 0) return null

            val songs = ((jsonResponse["data"] as Map<String, Any>)["song"] as Map<String, Any>)["list"] as List<Map<String, Any>>
            songs.find { it["songname"] as String == data.title }?.let {
                LYRIC_URL.format(it["songmid"] as String)
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
