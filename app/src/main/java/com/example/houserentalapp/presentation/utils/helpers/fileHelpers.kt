package com.example.houserentalapp.presentation.utils.helpers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import com.example.houserentalapp.domain.model.ImageSource
import java.io.File


fun getFileBitMapFromAbsPath(absolutePath: String): Bitmap? {
    val file = File(absolutePath)
    return if (!file.exists()) {
        Log.w("getFileBitMapFromAbsPath", "Image($absolutePath) is not exists")
        null
    }
    else
        BitmapFactory.decodeFile(file.absolutePath)
}

fun loadImageSourceToImageView(imageSource: ImageSource, imageView: ImageView) {
    when(imageSource) {
        is ImageSource.LocalFile -> {
            val bitmap = getFileBitMapFromAbsPath(imageSource.filePath) ?: run {
                Log.w("loadImageSourceToImageView", "image address not found")
                return
            }
            imageView.setImageBitmap(bitmap)
        }
        is ImageSource.Uri -> {
            imageView.setImageURI(imageSource.uri)
        }
    }
}