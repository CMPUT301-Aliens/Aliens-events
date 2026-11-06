package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.db.ImageDB
import com.example.campsitelotterysystem.model.UploadedImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageControllerTest {

    private class FakeImageDB : ImageDB {
        var images = listOf<UploadedImage>()
        val removedImages = mutableListOf<UploadedImage>()

        override fun getAllImages(onSuccess: (List<UploadedImage>) -> Unit, onFailure: (Exception) -> Unit) {
            onSuccess(images)
        }

        override fun removeImage(image: UploadedImage, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
            removedImages.add(image)
            onSuccess()
        }
    }

    @Test
    fun fetchAllImagesReturnsListFromDatabase() {
        val fakeDb = FakeImageDB()
        val controller = ImageController(fakeDb)

        val expectedList = listOf(
            UploadedImage(id = "img1", uploaderId = "user1"),
            UploadedImage(id = "img2", uploaderId = "user2")
        )
        fakeDb.images = expectedList

        var actualList: List<UploadedImage>? = null
        controller.fetchAllImages(
            onSuccess = { actualList = it },
            onFailure = { throw it }
        )

        assertEquals(expectedList, actualList)
        assertEquals(2, actualList?.size)
    }

    @Test
    fun removeImageCallsDatabaseRemove() {
        val fakeDb = FakeImageDB()
        val controller = ImageController(fakeDb)
        val image = UploadedImage(id = "img-99", uploaderId = "user-99")

        var successCalled = false
        controller.removeImage(
            image = image,
            onSuccess = { successCalled = true },
            onFailure = { throw it }
        )

        assertTrue(successCalled)
        assertEquals(1, fakeDb.removedImages.size)
        assertEquals("img-99", fakeDb.removedImages[0].id)
    }
}
