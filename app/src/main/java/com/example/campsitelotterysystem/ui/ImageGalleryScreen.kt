package com.example.campsitelotterysystem.ui

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.campsitelotterysystem.controller.ImageController
import com.example.campsitelotterysystem.db.FirestoreImageDB
import com.example.campsitelotterysystem.model.UploadedImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
fun ImageGalleryScreen(
    controller: ImageController = remember { ImageController(FirestoreImageDB()) }
) {
    var images by remember { mutableStateOf<List<UploadedImage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        controller.fetchAllImages(
            onSuccess = {
                images = it
                isLoading = false
            },
            onFailure = {
                isLoading = false
            }
        )
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (images.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No images found")
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(8.dp)
        ) {
            items(images) { image ->
                ImageItemCard(image)
            }
        }
    }
}

@Composable
fun ImageItemCard(image: UploadedImage) {
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(image.imageUrl) {
        if (image.imageUrl.isNotBlank()) {
            withContext(Dispatchers.IO) {
                try {
                    if (image.imageUrl.startsWith("http")) {
                        bitmap = BitmapFactory.decodeStream(URL(image.imageUrl).openStream())
                    } else if (image.imageUrl.length > 100) {
                        val bytes = Base64.decode(image.imageUrl.substringAfter(","), Base64.DEFAULT)
                        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }
                } catch (_: Exception) {
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image")
                }
            }

            Text("Image ID: ${image.id}")
            Text("Uploader ID: ${image.uploaderId}")
            Text("Timestamp: ${image.timestamp}")
        }
    }
}
