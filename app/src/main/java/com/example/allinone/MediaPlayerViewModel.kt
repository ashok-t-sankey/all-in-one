package com.example.allinone

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class MediaPlayerViewModel(private val context: Context) {
    val exoPlayer = ExoPlayer.Builder(context).build();
    var audioFile: AudioItem? = null;
    fun playAudio(audioItem: AudioItem? = null) {
        audioFile = audioItem;
        Log.d("MediaPlayerViewModel", "playAudio: ${audioItem?.path}")
        if (audioItem != null) {
            val mediaItem = MediaItem.Builder().setUri(Uri.parse(audioItem.path)).build()
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        } else {
            exoPlayer.play()
        }
    }

    fun pauseAudio() {
        exoPlayer.pause()
    }

    fun stopAudio() {
        exoPlayer.stop()
        exoPlayer.release()
    }
}