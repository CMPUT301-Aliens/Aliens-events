package com.example.campsitelotterysystem.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.example.campsitelotterysystem.ui.theme.CampsiteLotterySystemTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import com.example.campsitelotterysystem.controller.LotteryController
import com.example.campsitelotterysystem.db.FirestoreLotteryDB
import com.example.campsitelotterysystem.model.Lottery
import androidx.compose.ui.platform.LocalContext
import com.example.campsitelotterysystem.db.getCurrentUserId

/**
 * Purpose: The owner's screen for creating a lottery (#15, #17, #32).
 * Role: Opened by the Manage Lottery button with EXTRA_CAMPSITE_ID. Sends the lottery to
 * LotteryController and shows the result.
 * Outstanding issues: It only creates new lotteries, and can't load or edit a saved one yet.
 * It uses one campsite per lottery (#16 is not in this checkpoint).
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
    var registrationStart by remember { mutableStateOf<Date?>(null) }
    var registrationEnd by remember { mutableStateOf<Date?>(null) }
    var bookingStart by remember { mutableStateOf<Date?>(null) }
    var bookingEnd by remember { mutableStateOf<Date?>(null) }
    var statusMessage by remember { mutableStateOf("") }
    var savedLottery by remember { mutableStateOf<Lottery?>(null) }
    val lotteryController = remember { LotteryController(FirestoreLotteryDB()) }

    val formIsFilled = numberOfWinners.isNotBlank() &&
            registrationStart != null && registrationEnd != null &&
            bookingStart != null && bookingEnd != null

    Column(modifier = modifier
        .verticalScroll(rememberScrollState())
        .padding(16.dp)) {
        Text(text = "Lottery for campsite: $campsiteId")

        OutlinedTextField(
            value = numberOfWinners,
            onValueChange = { numberOfWinners = it },
            label = { Text("Number of winners") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)

        )
        DateTimeField("Registration start", registrationStart) { registrationStart = it }
        DateTimeField("Registration end", registrationEnd) { registrationEnd = it }
        DateTimeField("Booking start", bookingStart) { bookingStart = it }
        DateTimeField("Booking end", bookingEnd) { bookingEnd = it }
        Button(
            enabled = formIsFilled,
            onClick = {
                val lottery = Lottery(
                    ownerId = ownerId,
                    campsiteIds = listOfNotNull(campsiteId),
                    registrationStartDate = registrationStart!!,
                    registrationEndDate = registrationEnd!!,
                    bookingStartDate = bookingStart!!,
                    bookingEndDate = bookingEnd!!,
                    numberOfWinners = numberOfWinners.toIntOrNull() ?: 0
                )
                lotteryController.createLotteryListing(
                    lottery,
                    onSuccess = { id ->
                        statusMessage = "Saved lottery: $id"
                        savedLottery = lottery.copy(id = id)
                    },
                    onInvalid = { errors -> statusMessage = errors.joinToString("\n") },
                    onFailure = { error -> statusMessage = "Could not save: ${error.message}" }
                )
            }
        ) {
            Text("Save lottery")
        }

        Text(text = statusMessage)
        savedLottery?.let { saved ->
            Text("Number of winners: ${saved.numberOfWinners}")
            Text(
                "Registration: ${formatDateTime(saved.registrationStartDate)} to ${
                    formatDateTime(
                        saved.registrationEndDate
                    )
                }"
            )
            Text("Booking: ${formatDateTime(saved.bookingStartDate)} to ${formatDateTime(saved.bookingEndDate)}")
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
