package com.example.app.Data.repository

import com.example.app.Data.ContentResolverHelper
import com.example.app.Model.AudioModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AudioRepository @Inject constructor(private val contentResolverHelper: ContentResolverHelper) {
    suspend fun getAudioData():List<AudioModel> = withContext(Dispatchers.IO){
        contentResolverHelper.getAudioData()
    }
}