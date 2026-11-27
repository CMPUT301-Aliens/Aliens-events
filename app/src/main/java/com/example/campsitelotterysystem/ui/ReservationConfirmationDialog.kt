package com.example.campsitelotterysystem.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campsitelotterysystem.model.Reservation
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Purpose : Displays an on-screen confirmation popup after an entrant confirms a campsite reservation (US 01.05.01)
 * Role : Shows Booking ID, campsite name and location, check-in/check-out dates, total cost, and a "Done" button to dismiss
 * Outstanding issues : none
 */

@Composable
fun ReservationConfirmationDialog(
    reservation: Reservation,
    onDone: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val sortedDates = remember(reservation.reservedDates) {
        reservation.reservedDates.sorted()
    }

    val checkInText = sortedDates.firstOrNull()?.let { dateFormat.format(it) } ?: "N/A"
    val checkOutText = if (sortedDates.size > 1) {
        dateFormat.format(sortedDates.last())
    } else {
        checkInText
    }
    val formattedCost = String.format(Locale.US, "$%.2f", reservation.totalCost)

    AlertDialog(
        onDismissRequest = onDone,
        title = {
            Text(
                text = "Reservation Confirmed!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Your campsite stay has been booked. Here are your confirmation details:",
                    style = MaterialTheme.typography.bodyMedium
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text("Booking ID: ${reservation.id}", fontWeight = FontWeight.SemiBold)
                Text("Campsite: ${reservation.campsiteName}")
                Text("Location: ${reservation.campsiteLocation}")
                Text("Check-in Date: $checkInText")
                Text("Check-out Date: $checkOutText")
                Text("Total Cost: $formattedCost", fontWeight = FontWeight.SemiBold)
            }
        },
        confirmButton = {
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
        }
    )
}