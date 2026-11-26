package com.example.campsitelotterysystem.model

data class Review(
    val id: String = "",
    val campsiteId: String = "",
    val rating: Int = 0,
    val text: String = "",
    val reviewerName: String = "",
    val reviewerProfileImageUrl: String = ""
)
