package com.yausername.dvd.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yausername.dvd.model.VidInfoItem
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VidInfoViewModel : ViewModel() {

    val vidFormats: MutableLiveData<VideoInfo> = MutableLiveData()
    val loadState: MutableLiveData<LoadState> = MutableLiveData(LoadState.INITIAL)
    val thumbnail: MutableLiveData<String> = MutableLiveData()
    lateinit var selectedItem: VidInfoItem.VidFormatItem

    private fun submit(vidInfoItems: VideoInfo?) {
        vidFormats.postValue(vidInfoItems)
    }

    private fun updateLoading(loadState: LoadState) {
        this.loadState.postValue(loadState)
    }

    private fun updateThumbnail(thumbnail: String?) {
        this.thumbnail.postValue(thumbnail)
    }

    fun fetchInfo(url: String) {
        viewModelScope.launch {
            updateLoading(LoadState.LOADING)
            submit(null)
            updateThumbnail(null)
            lateinit var vidInfo: VideoInfo
            try {
                withContext(Dispatchers.IO) {
                    vidInfo = YoutubeDL.getInstance().getInfo(url)
                }
            } catch (e: Exception) {
                updateLoading(LoadState.LOADED)
                return@launch
            }

            updateLoading(LoadState.LOADED)
            updateThumbnail(vidInfo.thumbnail)
            submit(vidInfo)
        }
    }

}

enum class LoadState {
    INITIAL, LOADING, LOADED
}
