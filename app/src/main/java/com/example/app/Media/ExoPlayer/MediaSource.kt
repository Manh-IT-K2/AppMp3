package com.example.app.Media.ExoPlayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.example.app.Data.repository.AudioRepository
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import javax.inject.Inject

class MediaSource @Inject constructor(private val respository:AudioRepository) {
    private val onReadyListeners:MutableList<OnReadyListener> = mutableListOf()

    var audioMediaMetaData: List<MediaMetadataCompat> = emptyList()

    private var state: AudioSourceState = AudioSourceState.STATE_CREATED
        set(value) {
            if(value == AudioSourceState.STATE_CREATED || value == AudioSourceState.STATE_ERROR){
                synchronized(onReadyListeners){
                    field = value
                    onReadyListeners.forEach{ listener: OnReadyListener ->
                        listener.invoke(isReady)
                    }
                }
            }else{
                field = value
            }
        }

    suspend fun load(){
        state = AudioSourceState.STATE_INITTIALIZING
        val data = respository.getAudioData()
        audioMediaMetaData = data.map{ audio ->
            MediaMetadataCompat.Builder()
                .putString(
                    MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
                    audio.id.toString()
                ).putString(
                    MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST,
                    audio.artists
                ).putString(
                    MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
                    audio.uri.toString()
                ).putString(
                    MediaMetadataCompat.METADATA_KEY_TITLE,
                    audio.title
                ).putString(
                    MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE,
                    audio.disPlayName
                ).putLong(
                    MediaMetadataCompat.METADATA_KEY_DURATION,
                    audio.duration.toLong()
                ).build()
        }
        state = AudioSourceState.STATE_INITTIALIZED
    }

    fun asMediaSource(dataSource: CacheDataSource.Factory): ConcatenatingMediaSource{
        val concatingMediaSource = ConcatenatingMediaSource()
        audioMediaMetaData.forEach{ mediaMetadataCompat ->
            val mediaItem = MediaItem.fromUri(
                mediaMetadataCompat.
                getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)
            )

            val mediaSource = ProgressiveMediaSource.Factory(dataSource)
                .createMediaSource(mediaItem)

            concatingMediaSource.addMediaSource(mediaSource)
        }

        return concatingMediaSource
    }
    fun asMediaItem() = audioMediaMetaData.map{ metaData ->
        val description = MediaDescriptionCompat.Builder()
            .setTitle(metaData.description.title)
            .setMediaId(metaData.description.mediaId)
            .setSubtitle(metaData.description.subtitle)
            .setMediaUri(metaData.description.mediaUri)
            .build()
        MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
    }.toMutableList()

    fun refresh(){
        onReadyListeners.clear()
        state  = AudioSourceState.STATE_CREATED
    }

    fun whenReady (listener: OnReadyListener): Boolean {
        return if (state == AudioSourceState.STATE_CREATED || state == AudioSourceState.STATE_INITTIALIZING){
            onReadyListeners += listener
            false
        }else{
            listener.invoke(isReady)
            true
        }
    }
    private val isReady: Boolean get() = state == AudioSourceState.STATE_INITTIALIZED
}

enum class AudioSourceState{
    STATE_CREATED,
    STATE_INITTIALIZING,
    STATE_INITTIALIZED,
    STATE_ERROR,
}

typealias OnReadyListener = (Boolean) -> Unit