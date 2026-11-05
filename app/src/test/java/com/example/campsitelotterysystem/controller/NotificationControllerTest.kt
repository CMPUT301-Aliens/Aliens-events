package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.db.NotificationDB
import com.example.campsitelotterysystem.model.Notification
import com.example.campsitelotterysystem.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date


/** Unit tests for NotificationController's saving and reading, using a fake database. */
class NotificationControllerTest {

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

        override fun getIncomingNotifications(
            userId: String,
            onSuccess: (List<Notification>) -> Unit,
            onFailure: (Exception) -> Unit)
        {
            val notificationList = incoming.getOrPut(userId) { mutableListOf<Notification>() } //if key does not exist, create value of empty notification list
            onSuccess(notificationList)
        }

        override fun getOutgoingNotifications(
            userId: String,
            onSuccess: (List<Notification>) -> Unit,
            onFailure: (Exception) -> Unit)
        {
            val notificationList = outgoing.getOrPut(userId) { mutableListOf<Notification>() } //if key does not exist, create value of empty notification list
            onSuccess(notificationList)
        }


        override fun getLoggedNotifications(
            onSuccess: (List<Notification>) -> Unit,
            onFailure: (Exception) -> Unit)
        {
            onSuccess(all)
        }

    }

    private val newNotification = Notification(
        id = "abc123",
        timestamp = Date(),
        senderId = "SENDER-ID1",
        recipientIds = listOf("1-001", "1-002", "1-003"),
        messageContent = "Sorry, you were not selected",
        type = "lost",
        lotteryId = "LOTTO-ID"
    )
    private val newNotification2 = Notification(
        id = "def456",
        timestamp = Date(),
        senderId = "SENDER-ID2",
        recipientIds = listOf("1-001"),
        messageContent = "Congrats! You were selected.",
        type = "won",
        lotteryId = "LOTTO-ID2"
    )

    private val newNotification3 = Notification(
        id = "321cba",
        timestamp = Date(),
        senderId = "SENDER-ID1",
        recipientIds = listOf("1-004"),
        messageContent = "Congrats! You were selected.",
        type = "won",
        lotteryId = "LOTTO-ID1"
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
        notificationsEnabled = true,
        role = "entrant"
    )
    private val user4 = User(
        deviceId = "1-004",
        name = "Bart Grey",
        email = "bartgrey@fakemail.com",
        phoneNumber= "555-9876-999",
        profileImageUrl = "treephoto",
        notificationsEnabled = true,
        role = "entrant"
    )

    private val loserUserList : List<User> = listOf(user1, user2, user3)


    private val controller = NotificationController(FakeNotificationDB())

    @Test
    fun sendingNotificationDoesNotFail() {
        controller.sendNotification(recipients = loserUserList,
            senderId = "SENDER-ID1",
            notification = newNotification,
            onSuccess = { },
            onFailure = {throw it })
    }

    @Test
    fun canGetIncoming(){
        var user1Incoming: List<Notification> = emptyList()
        var user3Incoming: List<Notification> = emptyList()

        controller.sendNotification(recipients = loserUserList,
            senderId = "SENDER-ID1",
            notification = newNotification,
            onSuccess = { },
            onFailure = {throw it })
        controller.getIncomingNotifications(user1,
            onSuccess = {incoming -> user1Incoming = incoming},
            onFailure = {throw it}
        )
        controller.getIncomingNotifications(user3,
            onSuccess = {incoming -> user3Incoming = incoming},
            onFailure = {throw it}
        )

        assertEquals(listOf(newNotification), user1Incoming)
        assertEquals(listOf(newNotification), user3Incoming)

    }

    @Test
    fun optingOutWorks() {
        var user2Incoming: List<Notification>? = null
        controller.sendNotification(recipients = loserUserList,
            senderId = "SENDER-ID1",
            notification = newNotification,
            onSuccess = { },
            onFailure = {throw it })
        controller.getIncomingNotifications(user2,
            onSuccess = {incoming -> user2Incoming = incoming},
            onFailure = {throw it}
        )

        assertEquals(emptyList<Notification>(), user2Incoming)
        }

    @Test
    fun multipleIncomingNotificationsTest() {
        var user1Incoming: List<Notification>? = null
        controller.sendNotification(recipients = loserUserList,
            senderId = "SENDER-ID1",
            notification = newNotification,
            onSuccess = { },
            onFailure = {throw it })
        controller.sendNotification(recipients = listOf(user1),
            senderId = "SENDER-ID2",
            notification = newNotification2,
            onSuccess = { },
            onFailure = {throw it })
        controller.getIncomingNotifications(user1,
            onSuccess = {incoming -> user1Incoming = incoming},
            onFailure = {throw it}
        )

        assertEquals(listOf(newNotification,  newNotification2), user1Incoming)
    }

    @Test
    fun multipleOutgoingNotificationsTest() {
        var senderOutgoing : List<Notification>? = null
        controller.sendNotification(recipients = loserUserList,
            senderId = "SENDER-ID1",
            notification = newNotification,
            onSuccess = { },
            onFailure = {throw it })
        controller.sendNotification(recipients = listOf(user4),
            senderId = "SENDER-ID1",
            notification = newNotification3,
            onSuccess = { },
            onFailure = {throw it })
        controller.getOutgoingNotifications("SENDER-ID1",
            onSuccess = {outgoing -> senderOutgoing = outgoing},
            onFailure = {throw it}
        )

        assertEquals(listOf(newNotification, newNotification3), senderOutgoing)
    }

    @Test
    fun multipleLoggedNotificationsTest() {
        var log : List<Notification>? = null
        controller.sendNotification(recipients = loserUserList,
            senderId = "SENDER-ID1",
            notification = newNotification,
            onSuccess = { },
            onFailure = {throw it })
        controller.sendNotification(recipients = listOf(user4),
            senderId = "SENDER-ID1",
            notification = newNotification3,
            onSuccess = { },
            onFailure = {throw it })
        controller.sendNotification(recipients = listOf(user1),
            senderId = "SENDER-ID2",
            notification = newNotification2,
            onSuccess = { },
            onFailure = {throw it })
        controller.getLoggedNotifications(
            onSuccess = {notifLog -> log = notifLog},
            onFailure = {throw it}
        )

        assertEquals(listOf(newNotification,  newNotification3, newNotification2), log)
    }

    }




