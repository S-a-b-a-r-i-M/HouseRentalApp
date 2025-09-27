package com.example.houserentalapp.presentation.utils.helpers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
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
            Glide.with(imageView.context)
                .load(imageSource.uri)
                .into(imageView)
        }
    }
}

// TODO-DOUBT: How Glide is optimized ?
fun loadImageSourceToImageViewV2(imageSource: ImageSource, imageView: ImageView) {
    val source = when(imageSource) {
        is ImageSource.LocalFile -> imageSource.filePath
        is ImageSource.Uri -> imageSource.uri
    }

    Glide.with(imageView.context)
        .load(source)
        .into(imageView)
}