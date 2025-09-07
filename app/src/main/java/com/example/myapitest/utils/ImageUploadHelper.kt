package com.example.myapitest.utils

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

object ImageUploadHelper {

    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Faz upload de uma imagem para Firebase Storage.
     *
     * @param imageUri URI da imagem selecionada
     * @param onResult Callback com URL de download ou erro
     */
    fun uploadImage(imageUri: Uri, onResult: (downloadUrl: String?, error: Exception?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onResult(null, Exception("Usuário não logado"))
            return
        }

        val imageRef: StorageReference = storage.reference
            .child("users/${user.uid}/cars/${java.util.UUID.randomUUID()}.jpg")

        val uploadTask = imageRef.putFile(imageUri)
        uploadTask
            .addOnSuccessListener {
                // Obter URL de download após upload
                imageRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        onResult(uri.toString(), null)
                    }
                    .addOnFailureListener { e ->
                        onResult(null, e)
                    }
            }
            .addOnFailureListener { e ->
                onResult(null, e)
            }
    }

    /**
     * Deleta uma imagem do Firebase Storage.
     *
     * @param imageUrl URL da imagem a ser deletada
     * @param onResult Callback com sucesso/falha
     */
    fun deleteImage(imageUrl: String, onResult: (success: Boolean) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onResult(false)
            return
        }

        try {
            val imageRef = storage.getReferenceFromUrl(imageUrl)
            imageRef.delete()
                .addOnSuccessListener { onResult(true) }
                .addOnFailureListener { onResult(false) }
        } catch (e: Exception) {
            onResult(false)
        }
    }
}
