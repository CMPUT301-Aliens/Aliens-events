package com.example.campsitelotterysystem.db

import com.example.campsitelotterysystem.model.UploadedImage

interface ImageDB {
    fun getAllImages(onSuccess: (List<UploadedImage>) -> Unit, onFailure: (Exception) -> Unit)
    fun addImage(image: UploadedImage, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit)
    fun removeImage(imageId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}

class FirestoreImageDB : ImageDB {
    private val imagesCollection = DBConnector.db.collection("images")

    override fun getAllImages(onSuccess: (List<UploadedImage>) -> Unit, onFailure: (Exception) -> Unit) {
        imagesCollection.get()
            .addOnSuccessListener { documents ->
                onSuccess(documents.toObjects(UploadedImage::class.java))
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    override fun addImage(image: UploadedImage, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val document = imagesCollection.document()
        val newImage = image.copy(id = document.id)
        document.set(newImage)
            .addOnSuccessListener { onSuccess(document.id) }
            .addOnFailureListener { onFailure(it) }
    }

    override fun removeImage(imageId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        imagesCollection.document(imageId).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
