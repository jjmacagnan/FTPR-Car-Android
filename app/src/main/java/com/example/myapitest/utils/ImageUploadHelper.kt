package com.example.myapitest.utils

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

object ImageUploadHelper {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    fun uploadImage(imageUri: Uri, callback: (String?, Exception?) -> Unit) {
        val filename = "car_images/${UUID.randomUUID()}.jpg"
        val imageRef = storageRef.child(filename)

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    callback(uri.toString(), null)
                }
            }
            .addOnFailureListener { exception ->
                callback(null, exception)
            }
    }
}