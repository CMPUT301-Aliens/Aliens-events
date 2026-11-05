package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.db.ImageDB
import com.example.campsitelotterysystem.model.UploadedImage

class ImageController(private val imageDB: ImageDB) {

    fun fetchAllImages(
        onSuccess: (List<UploadedImage>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        imageDB.getAllImages(onSuccess, onFailure)
    }

    fun removeImage(
        image: UploadedImage,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        imageDB.removeImage(image, onSuccess, onFailure)
    }
}
