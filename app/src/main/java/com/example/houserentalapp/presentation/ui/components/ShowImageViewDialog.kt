package com.example.houserentalapp.presentation.ui.components

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.widget.ImageView
import com.example.houserentalapp.R
import com.example.houserentalapp.domain.model.ImageSource
import com.example.houserentalapp.presentation.utils.helpers.getFileBitMapFromAbsPath


fun Context.showImageDialog(imageSource: ImageSource) {
    val dialog = Dialog(this)
    dialog.setContentView(R.layout.dialog_image)

    val imageView = dialog.findViewById<ImageView>(R.id.dialog_image)
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

    // Make dialog dismissible by clicking outside
    dialog.setCancelable(true)
    dialog.show()
}
