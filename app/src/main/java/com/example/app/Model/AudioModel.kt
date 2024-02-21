package com.example.app.Model

import android.net.Uri

data class AudioModel(
    val uri : Uri,
    val disPlayName : String,
    val id : Long,
    val artists: String,
    val data : String,
    val duration : Int,
    val title : String
)
