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

    fun addImage(
        image: UploadedImage,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        imageDB.addImage(image, onSuccess, onFailure)
    }
}
