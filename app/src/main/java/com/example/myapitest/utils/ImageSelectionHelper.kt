package com.example.myapitest.utils

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

class ImageSelectionHelper(
    private val activity: AppCompatActivity,
    private val onImageSelected: (Uri) -> Unit,
    private val onError: (String) -> Unit
) {

    private var cameraImageUri: Uri? = null

    private val imagePickerLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { onImageSelected(it) }
        }

    private val cameraLauncher: ActivityResultLauncher<Uri> =
        activity.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                cameraImageUri?.let { onImageSelected(it) }
            } else {
                onError("Falha ao tirar foto")
            }
        }

    private val cameraPermissionLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                onError("Permissão da câmera é necessária")
            }
        }

    fun showImagePickerDialog() {
        val options = arrayOf("Escolher da Galeria", "Tirar Foto")
        AlertDialog.Builder(activity)
            .setTitle("Selecionar Imagem")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> selectFromGallery()
                    1 -> requestCameraPermission()
                }
            }
            .show()
    }

    private fun selectFromGallery() {
        imagePickerLauncher.launch("image/*")
    }

    private fun requestCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun openCamera() {
        try {
            val imageFile = File.createTempFile(
                "car_${UUID.randomUUID()}",
                ".jpg",
                activity.cacheDir
            )

            cameraImageUri = FileProvider.getUriForFile(
                activity,
                "${activity.packageName}.fileprovider",
                imageFile
            )

            cameraImageUri?.let { uri ->
                cameraLauncher.launch(uri)
            }
        } catch (e: Exception) {
            onError("Erro ao criar arquivo temporário: ${e.message}")
        }
    }

    companion object {
        fun create(
            activity: AppCompatActivity,
            onImageSelected: (Uri) -> Unit,
            onError: (String) -> Unit = {}
        ): ImageSelectionHelper {
            return ImageSelectionHelper(activity, onImageSelected, onError)
        }
    }
}