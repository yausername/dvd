package org.yausername.dvd.model

import com.yausername.youtubedl_android.mapper.VideoFormat
import com.yausername.youtubedl_android.mapper.VideoInfo

sealed class VidInfoItem {
    abstract val id: String


    data class VidFormatItem(val vidInfo: VideoInfo, val formatId: String) : VidInfoItem() {
        override val id = vidInfo.id + "_" + formatId
        val vidFormat: VideoFormat = vidInfo.formats.find { f -> f.formatId == formatId }!!
    }

    data class VidHeaderItem(val vidInfo: VideoInfo) : VidInfoItem() {
        override val id = vidInfo.id
    }
}