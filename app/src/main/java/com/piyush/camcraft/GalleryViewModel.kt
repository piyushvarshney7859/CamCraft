package com.piyush.camcraft

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Data class to hold our image URI
data class CapturedImage(val uri: Uri, val dateAdded: Long)

class GalleryViewModel(application: Application) : AndroidViewModel(application) {
    private val _images = MutableStateFlow<List<CapturedImage>>(emptyList())
    val images: StateFlow<List<CapturedImage>> = _images.asStateFlow()

    fun loadImages() {
        val imageList = mutableListOf<CapturedImage>()

        // We only want the ID and the Date Added
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED
        )

        // Filter to only show images from the "CamCraft" folder
        val selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf("CamCraft")

        // Show newest photos first
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val context = getApplication<Application>().applicationContext

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                // Convert the ID into a usable Image URI
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                imageList.add(CapturedImage(contentUri, dateAdded))
            }
        }
        _images.value = imageList
    }
}