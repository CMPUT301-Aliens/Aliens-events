package com.example.campsitelotterysystem.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.campsitelotterysystem.controller.LotteryController
import com.example.campsitelotterysystem.db.FirestoreLotteryDB
import com.example.campsitelotterysystem.db.FirestoreUserDB
import com.example.campsitelotterysystem.db.getCurrentUserId
import com.example.campsitelotterysystem.model.Lottery
import com.example.campsitelotterysystem.ui.theme.CampsiteLotterySystemTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import com.example.campsitelotterysystem.controller.CampsiteController
import com.example.campsitelotterysystem.db.FirestoreCampsiteDB
import com.example.campsitelotterysystem.model.Campsite

/**
 * Purpose: The owner's screen for creating, viewing, and editing a lottery (#15, #17, #32, #16, #33, #34).
 * Role: Opened by the Manage Lottery button with EXTRA_CAMPSITE_ID. The owner can add more of
 * their campsites to the lottery and edit it until registration ends. Sends the lottery to
 * LotteryController and shows the result. View Waitlist opens LotteryManagementActivity.
 * Outstanding issues: Past dates are only blocked when editing, not when creating a lottery.
 */
class LotteryEditActivity : ComponentActivity() {
    companion object {
        const val EXTRA_CAMPSITE_ID = "campsiteId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val campsiteId = intent.getStringExtra(EXTRA_CAMPSITE_ID)
        setContent {
            CampsiteLotterySystemTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LotteryEditScreen(
                        campsiteId = campsiteId,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

}

@Composable
fun LotteryEditScreen(campsiteId: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val ownerId = remember { getCurrentUserId(context) }
    var numberOfWinners by remember { mutableStateOf("") }
    var waitlistCapacity by remember { mutableStateOf("") }
    var registrationStart by remember { mutableStateOf<Date?>(null) }
    var registrationEnd by remember { mutableStateOf<Date?>(null) }
    var bookingStart by remember { mutableStateOf<Date?>(null) }
    var bookingEnd by remember { mutableStateOf<Date?>(null) }
    var statusMessage by remember { mutableStateOf("") }
    var savedLottery by remember { mutableStateOf<Lottery?>(null) }
    var isLoading by remember { mutableStateOf(campsiteId != null) }
    val lotteryController = remember { LotteryController(FirestoreLotteryDB(), FirestoreUserDB()) }
    var isSaving by remember { mutableStateOf(false) }
    val campsiteController = remember { CampsiteController(FirestoreCampsiteDB()) }
    var ownerCampsites by remember { mutableStateOf<List<Campsite>>(emptyList()) }
    var takenCampsiteIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedCampsiteIds by remember { mutableStateOf(setOfNotNull(campsiteId)) }
    var isEditing by remember { mutableStateOf(false) }
    LaunchedEffect(campsiteId) {
        if (campsiteId != null) {
            lotteryController.getLotteryForCampsite(
                campsiteId,
                onSuccess = { lottery ->
                    savedLottery = lottery
                    isLoading = false

                },
                onFailure = { error ->
                    statusMessage = "Could not load: ${error.message}"
                    isLoading = false

                }
            )
        }
    }

    // #16: load the owner's campsites, then find which are already in a lottery.
    LaunchedEffect(ownerId) {
        campsiteController.getOwnerCampsites(
            ownerId,
            onSuccess = { campsites ->
                ownerCampsites = campsites
                lotteryController.findCampsitesInALottery(
                    campsites.map { it.id },
                    onSuccess = { taken -> takenCampsiteIds = taken },
                    onFailure = { error -> statusMessage = "Could not check your campsites: ${error.message}" }
                )
            },
            onFailure = { error -> statusMessage = "Could not load your campsites: ${error.message}" }
        )
    }


    val formIsFilled = numberOfWinners.isNotBlank() &&
            registrationStart != null && registrationEnd != null &&
            bookingStart != null && bookingEnd != null

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(text = "Lottery for campsite: ${lotteryController.campsiteNames(listOfNotNull(campsiteId), ownerCampsites).joinToString()}")
        if (isLoading) {
            Text("Loading...")
        }
        if (!isLoading && (savedLottery == null || isEditing)) {
            // #16: the owner picks which of their campsites go in this lottery.
            Text("Campsites in this lottery", style = MaterialTheme.typography.titleMedium)
            ownerCampsites.forEach { campsite ->
                val isThisCampsite = campsite.id == campsiteId
                // #33: when editing, this lottery's own campsites aren't "taken".
                val inThisLottery = campsite.id in (savedLottery?.campsiteIds ?: emptyList())
                val isTaken = campsite.id in takenCampsiteIds && !inThisLottery
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = campsite.id in selectedCampsiteIds,
                        enabled = !isThisCampsite && !isTaken,
                        onCheckedChange = { checked ->
                            selectedCampsiteIds = if (checked) selectedCampsiteIds + campsite.id else selectedCampsiteIds - campsite.id
                        }
                    )
                    Text(
                        when {
                            isThisCampsite -> "${campsite.name} (this campsite)"
                            isTaken -> "${campsite.name} (already in a lottery)"
                            else -> campsite.name
                        }
                    )
                }
            }

            OutlinedTextField(
                value = numberOfWinners,
                onValueChange = { numberOfWinners = it },
                label = { Text("Number of winners") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            // #34: optional limit on the waitlist. Leave it blank for no limit.
            OutlinedTextField(
                value = waitlistCapacity,
                onValueChange = { waitlistCapacity = it },
                label = { Text("Waitlist capacity (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            DateTimeField("Registration start", registrationStart) { registrationStart = it }
            DateTimeField("Registration end", registrationEnd) { registrationEnd = it }
            DateTimeField("Booking start", bookingStart) { bookingStart = it }
            DateTimeField("Booking end", bookingEnd) { bookingEnd = it }
            Button(
                enabled = formIsFilled && !isSaving,
                onClick = {
                    val lottery = Lottery(
                        ownerId = ownerId,
                        campsiteIds = selectedCampsiteIds.toList(),
                        registrationStartDate = registrationStart!!,
                        registrationEndDate = registrationEnd!!,
                        bookingStartDate = bookingStart!!,
                        bookingEndDate = bookingEnd!!,
                        numberOfWinners = numberOfWinners.toIntOrNull() ?: 0,
                        // #34: blank means no limit. Anything that isn't a whole number becomes 0, so it shows an error.
                        waitlistCapacity = if (waitlistCapacity.isBlank()) null else waitlistCapacity.trim().toIntOrNull() ?: 0
                    )
                    isSaving = true
                    val saved = savedLottery
                    if (isEditing && saved != null) {
                        // #33: save the owner's changes to the existing lottery.
                        lotteryController.editLotteryListing(
                            saved,
                            lottery,
                            Date(),
                            onSuccess = { updated ->
                                savedLottery = updated
                                isEditing = false
                                isSaving = false
                                statusMessage = "Changes saved."
                            },
                            onInvalid = { errors ->
                                statusMessage = errors.joinToString("\n")
                                isSaving = false
                            },
                            onFailure = { error ->
                                statusMessage = "Could not save changes: ${error.message}"
                                isSaving = false
                            }
                        )
                    } else {
                        lotteryController.createLotteryListing(
                            lottery,
                            onSuccess = { id ->
                                statusMessage = "Saved lottery: $id"
                                savedLottery = lottery.copy(id = id)
                                isSaving = false
                            },
                            onInvalid = { errors ->
                                statusMessage = errors.joinToString("\n")
                                isSaving = false
                            },
                            onFailure = { error ->
                                statusMessage = "Could not save: ${error.message}"
                                isSaving = false
                            }
                        )
                    }
                }
            ) {
                Text(if (isEditing) "Save Changes" else "Publish Lottery Listing")
            }
            if (isEditing) {
                OutlinedButton(onClick = {
                    isEditing = false
                    statusMessage = ""
                }) {
                    Text("Cancel")
                }
            }
        }
        Text(text = statusMessage)
        savedLottery?.takeIf { !isEditing }?.let { saved ->
            Text("Campsites: ${lotteryController.campsiteNames(saved.campsiteIds, ownerCampsites).joinToString(", ")}")
            Text("Number of winners: ${saved.numberOfWinners}")
            Text("Waitlist capacity: ${saved.waitlistCapacity ?: "No limit"}")
            Text("Registration: ${formatDateTime(saved.registrationStartDate)} to ${formatDateTime(saved.registrationEndDate)}")
            Text("Booking: ${formatDateTime(saved.bookingStartDate)} to ${formatDateTime(saved.bookingEndDate)}")
            Button(onClick = {
                context.startActivity(
                    Intent(context, LotteryManagementActivity::class.java)
                        .putExtra(LotteryManagementActivity.EXTRA_CAMPSITE_ID, campsiteId)
                )
            }) {
                Text("View Waitlist")
            }
            // #33: the owner can change the lottery until registration ends.
            if (saved.canBeEdited(Date())) {
                OutlinedButton(onClick = {
                    numberOfWinners = saved.numberOfWinners.toString()
                    waitlistCapacity = saved.waitlistCapacity?.toString() ?: ""
                    registrationStart = saved.registrationStartDate
                    registrationEnd = saved.registrationEndDate
                    bookingStart = saved.bookingStartDate
                    bookingEnd = saved.bookingEndDate
                    selectedCampsiteIds = saved.campsiteIds.toSet()
                    statusMessage = ""
                    isEditing = true
                }) {
                    Text("Edit Lottery")
                }
            } else {
                Text("This lottery can't be changed because registration has ended.")
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeField(label: String, value: Date?, onValueChange: (Date) -> Unit) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState()
    val timeState = rememberTimePickerState()

    OutlinedButton(onClick = { showDatePicker = true }) {
        Text(if (value == null) label else "$label: ${formatDateTime(value)}")
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    enabled = dateState.selectedDateMillis != null,
                    onClick = {
                        showDatePicker = false
                        showTimePicker = true
                    }
                ) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(label) },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    val dateMillis = dateState.selectedDateMillis
                    if (dateMillis != null) {
                        onValueChange(
                            combineDateAndTime(
                                dateMillis,
                                timeState.hour,
                                timeState.minute
                            )
                        )
                    }
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        )
    }
}

fun combineDateAndTime(dateMillis: Long, hour: Int, minute: Int): Date {
    val pickedDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    pickedDate.timeInMillis = dateMillis

    val result = Calendar.getInstance()
    result.clear()
    result.set(
        pickedDate.get(Calendar.YEAR),
        pickedDate.get(Calendar.MONTH),
        pickedDate.get(Calendar.DAY_OF_MONTH),
        hour,
        minute
    )
    return result.time
}

fun formatDateTime(date: Date): String {
    return SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(date)
}
