package com.example.app.Data

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.provider.MediaStore.Audio
import android.util.Log
import androidx.annotation.WorkerThread
import com.example.app.Model.AudioModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ContentResolverHelper @Inject constructor(@ApplicationContext val context: Context) {
    private var mCursor: Cursor? = null

    private val projection:Array<String> = arrayOf(
        Audio.AudioColumns.DISPLAY_NAME,
        Audio.AudioColumns._ID,
        Audio.AudioColumns.ARTIST,
        Audio.AudioColumns.DATA,
        Audio.AudioColumns.DURATION,
        Audio.AudioColumns.TITLE,
    )

    private var selectionClause:String? = "${Audio.AudioColumns.IS_MUSIC} = ?"
    private var selectionArq = arrayOf("1")
    private val sortOrder = "${Audio.AudioColumns.DISPLAY_NAME} ASC"

    @WorkerThread
    fun getAudioData():List<AudioModel>{
        return getCursorData();
    }

    private fun getCursorData(): MutableList<AudioModel>{
        val audioList = mutableListOf<AudioModel>()

        mCursor = context.contentResolver.query(
            Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selectionClause,
            selectionArq,
            sortOrder
        )

        mCursor?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(Audio.AudioColumns._ID)
            val displayNameColumn = cursor.getColumnIndexOrThrow(Audio.AudioColumns.DISPLAY_NAME)
            val artistColumn = cursor.getColumnIndexOrThrow(Audio.AudioColumns.ARTIST)
            val dataColumn = cursor.getColumnIndexOrThrow(Audio.AudioColumns.DATA)
            val durationColumn = cursor.getColumnIndexOrThrow(Audio.AudioColumns.DURATION)
            val titleColumn = cursor.getColumnIndexOrThrow(Audio.AudioColumns.TITLE)

            cursor.apply {
                if( count == 0){
                    Log.e("Cursor", "getCurosrData: Cursor is Empty")
                }else{
                    while(cursor.moveToNext()){
                        val displayName = getString(displayNameColumn)
                        val id = getLong(idColumn)
                        val artist = getString(artistColumn)
                        val data = getString(dataColumn)
                        val duration = getInt(durationColumn)
                        val title = getString(titleColumn)
                        val uri = ContentUris.withAppendedId(
                            Audio.Media.EXTERNAL_CONTENT_URI, id
                        )

                        audioList += AudioModel(
                            uri, displayName, id, artist,data, duration,title
                        )
                    }
                }
            }
        }
        return audioList;
    }
}