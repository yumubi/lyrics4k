package io.goji.lyrics4k.model

/**
 * Data class representing media information. title is required, all other fields are optional.
 */
data class MediaData(
    val title: String,
    val artist: String = "",
    val album: String = "",
    val duration: Int = 0
)
