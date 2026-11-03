package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.db.NotificationDB
import com.example.campsitelotterysystem.model.Notification


class NotificationController (private val notificationDB: NotificationDB){
    // The following use of maps is adapted from Anthropic, Claude, "kotlin dictionary where key is string and value is array of custom class that can be appended to easily", 2026-11-02
    private val receivedNotifications = mutableMapOf<String, MutableList<Notification>>()
    private val sentNotifications = mutableMapOf<String, MutableList<Notification>>()

    fun sendNotification(recipientIds: List<String>, senderId: String,  notification: Notification) {








    }




}