package com.example.houserentalapp.presentation.utils.helpers

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.houserentalapp.presentation.utils.extensions.deleteFile
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.extensions.logError
import java.io.File

class ImageUploadHelper {
    private lateinit var photoUri: Uri
    var multipleImagesFromPicker: Boolean = false

    // Launchers - initialized per fragment/activity
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>

    // Init With Fragment
    fun init(
        fragment: Fragment,
        onImageFromPicker: (Intent) -> Unit,
        onImageFromCamera: (Uri) -> Unit,
        onPermissionDenied: () -> Unit = {}
    ): ImageUploadHelper {
        val context = fragment.requireContext()

        /*
         1. Callback is used immediately in lambda, not stored as class property
         2. Not holding any fragment references So no memory leak
         */

        // Image Picker Launcher
        imagePickerLauncher = fragment.registerForActivityResult( // How Fragments's registerForActivityResult diff from Activity's
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK)
                result.data?.let {  onImageFromPicker(it) }
        }

        // Camera Launcher
        cameraLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success)
                onImageFromCamera(photoUri)
            else { // Delete the image file from cache_dir
                logDebug("Image Upload via camera is cancelled.")
                val path = photoUri.path ?: return@registerForActivityResult
                path.deleteFile()
            }
        }

        // PermissionLauncher
        cameraPermissionLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted)
                openCamera(context)
            else
                onPermissionDenied()
        }
        return this
    }

    private fun openCamera(context: Context) {
        val photoFile = File.createTempFile("IMG_TEMP_", ".jpg", context.cacheDir)
        photoUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            photoFile
        )
        cameraLauncher.launch(photoUri)
    }

    fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            if (multipleImagesFromPicker) {
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
        }
        imagePickerLauncher.launch(intent)
    }

    fun checkCameraPermissionAndOpenCamera(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        )
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        else
            openCamera(context)
    }

    fun showAddImageOptions(context: Context) {
        val options = arrayOf("Camera", "Gallery")
        AlertDialog.Builder(context)
            .setTitle("Add Image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpenCamera(context)
                    1 -> openImagePicker()
                }
            }
            .show()
    }

    fun getCameraPhotoUri() = if(::photoUri.isInitialized) photoUri else null
}