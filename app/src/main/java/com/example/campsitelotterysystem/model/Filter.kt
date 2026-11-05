package com.example.campsitelotterysystem.model

import java.util.Date

data class Filter(
    val location: String = "",
    val date: Date? = null,
    val numberOfGuests: Int? = null
)
