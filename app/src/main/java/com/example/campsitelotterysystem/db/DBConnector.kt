package com.example.campsitelotterysystem.db

/**
 * Purpose : Connects DB classes to Firestore. Gives the app shared connection to Firestore database.
 * Role : Collaborates with CampsiteDB , LotteryDB , UserDB, NotificationDB , ReservationDB, MessageDB. Used by all DB classes to read and write data
 * Design Pattern : Every DB class uses the same connection
 * Outstanding issues : Firestore security rules are open for testing until January 31, 2027
 */


import com.google.firebase.firestore.FirebaseFirestore

object DBConnector {
    /**
     * Shared Firestore instance used by all DB classes
     */
    val db = FirebaseFirestore.getInstance()
}