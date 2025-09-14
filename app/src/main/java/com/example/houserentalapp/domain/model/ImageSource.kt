package com.example.houserentalapp.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class ImageSource : Parcelable {
    @Parcelize
    data class LocalFile(val filePath: String) : ImageSource()
    @Parcelize
    data class Uri(val uri: android.net.Uri) : ImageSource()
}