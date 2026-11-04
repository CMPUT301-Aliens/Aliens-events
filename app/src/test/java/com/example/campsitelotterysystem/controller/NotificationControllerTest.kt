package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.db.NotificationDB
import com.example.campsitelotterysystem.model.Notification
import com.example.campsitelotterysystem.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date


/** Unit tests for NotificationController's saving and reading, using a fake database. */
/** Tests for reading not implemented yet as said methods are not implemented in NotificationController*/
class LotteryControllerTest {

    /** A pretend database that remembers what it was asked to save. */
    private class FakeNotificationDB : NotificationDB {
        val incoming = mutableMapOf<String, MutableList<Notification>>()
        val outgoing = mutableMapOf<String, MutableList<Notification>>()
        val all = mutableListOf<Notification>()

        override fun saveIncomingNotification(
            recipientId: String,
            notification: Notification,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit)
         {
             val notificationList = incoming.getOrPut(recipientId) { mutableListOf<Notification>() } //if key does not exist, create value of empty notification list
             notificationList.add(notification)
             onSuccess()
        }

        override fun saveOutgoingNotification(
            senderId: String,
            notification: Notification,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit)
        {
            val notificationList = outgoing.getOrPut(senderId) { mutableListOf<Notification>() } //if key does not exist, create value of empty notification list
            notificationList.add(notification)
            onSuccess()
        }

        override fun logNotification(
            notification: Notification,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit)
        {
            all.add(notification)
            onSuccess()
        }

    }

    private val newNotification = Notification(
        id = "abc123",
        timestamp = Date(),
        senderId = "SENDER-ID",
        recipientIds = listOf("1-001", "1-002", "1-003"),
        messageContent = "Sorry, you were not selected",
        type = "lost",
        lotteryId = "LOTTO-ID"
    )




    private val user1 = User(
        deviceId = "1-001",
        name = "John Smith",
        email = "jsmith@fakemail.com",
        phoneNumber= "555-0101-999",
        profileImageUrl = "catphoto",
        notificationsEnabled = true,
        role = "entrant"
        )
    private val user2 = User(
        deviceId = "1-002",
        name = "Amber Doe",
        email = "j.doe@fakemail.com",
        phoneNumber= "555-0203-999",
        profileImageUrl = "dogphoto",
        notificationsEnabled = false,
        role = "entrant"
    )
    private val user3 = User(
        deviceId = "1-003",
        name = "Lisa Grey",
        email = "lisagrey@fakemail.com",
        phoneNumber= "555-0143-999",
        profileImageUrl = "fishphoto",
        notificationsEnabled = false,
        role = "entrant"
    )

    private val userList : List<User> = listOf(user1, user2, user3)


    private val controller = NotificationController(FakeNotificationDB())

    @Test
    fun sendingNotificationHasNoErrors() {
        controller.sendNotification(recipients = userList,
            senderId = "SENDER-ID",
            notification = newNotification,
            onSuccess = { },
            onFailure = {throw it })


    }
    }

