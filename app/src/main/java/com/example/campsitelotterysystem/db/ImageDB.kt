package com.example.campsitelotterysystem.db

import com.example.campsitelotterysystem.model.UploadedImage

interface ImageDB {
    fun getAllImages(onSuccess: (List<UploadedImage>) -> Unit, onFailure: (Exception) -> Unit)
    fun removeImage(image: UploadedImage, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}

class FirestoreImageDB : ImageDB {
    private val imagesCollection = DBConnector.db.collection("images")

    override fun getAllImages(onSuccess: (List<UploadedImage>) -> Unit, onFailure: (Exception) -> Unit) {
        imagesCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val list = querySnapshot.documents.mapNotNull { doc ->
                    val img = doc.toObject(UploadedImage::class.java)
                    img?.copy(id = doc.id)
                }
                onSuccess(list)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    override fun removeImage(image: UploadedImage, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        if (image.id.isNotBlank()) {
            imagesCollection.document(image.id).delete()
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        } else {
            onSuccess()
        }
    }
}
