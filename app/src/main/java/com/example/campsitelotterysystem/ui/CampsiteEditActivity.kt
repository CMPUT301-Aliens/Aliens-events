package com.example.campsitelotterysystem.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.example.campsitelotterysystem.controller.CampsiteController
import com.example.campsitelotterysystem.db.FirestoreCampsiteDB
import com.example.campsitelotterysystem.db.getCurrentUserId
import com.example.campsitelotterysystem.model.Campsite
import com.example.campsitelotterysystem.ui.theme.CampsiteLotterySystemTheme
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import com.google.firebase.firestore.GeoPoint



class CampsiteEditActivity : ComponentActivity() {
    companion object {
        const val EXTRA_CAMPSITE_ID = "campsiteID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val controller = CampsiteController(FirestoreCampsiteDB())
        val ownerId = getCurrentUserId(this)
        val campsiteId = intent.getStringExtra(EXTRA_CAMPSITE_ID)

        setContent {
            CampsiteLotterySystemTheme {
                CampsiteEditScreen(controller, ownerId, campsiteId, onDone = {finish() })
            }
        }
    }
}


@Composable
fun CampsiteEditScreen(
    controller: CampsiteController,
    ownerId: String,
    campsiteId : String?,
    onDone: () -> Unit
){
    val context = LocalContext.current
    val isNew = campsiteId == null

    var name by remember {mutableStateOf("")}
    var location by remember {mutableStateOf("")}
    var description by remember {mutableStateOf("")}
    var maxOcc by remember {mutableStateOf("")}
    var price by remember {mutableStateOf("")}
    var tags by remember {mutableStateOf("")}
    var rules by remember {mutableStateOf("")}
    var latitude by remember {mutableStateOf("")}
    var longitude by remember {mutableStateOf("")}


    var loaded by remember {mutableStateOf(Campsite())}
    var editing by remember {mutableStateOf(isNew)}
    var errorText by remember {mutableStateOf("")}
    var isSaving by remember {mutableStateOf(false)}


    LaunchedEffect(campsiteId) {
        if (campsiteId != null) {
            controller.getCampsite(
                            campsiteId,
                            onSuccess = {found -> if (found != null) {
                                loaded = found
                                name = found.name
                                location= found.location
                                description = found.description
                                maxOcc = found.maximumOccupancy.toString()
                                price = found.pricePerNight.toString()
                                tags = found.tags.joinToString(",")
                                rules = found.rulesAndPolicy
                                latitude = found.coordinates?.latitude?.toString() ?: ""
                                longitude = found.coordinates?.longitude?.toString() ?: ""
                            } else {
                                errorText = "Campsite not found"
                            }},
                            onFailure = { errorText = "couldn't load campsite."}
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ){
        Text(
            text = if (isNew) "Create Campsite" else "Campsite Details",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = name , onValueChange = {name = it},
        label = {Text("Campsite name")}, enabled = editing , modifier = Modifier.fillMaxWidth())

        OutlinedTextField(value = location , onValueChange = {location = it},
            label = {Text("Location")}, enabled = editing , modifier = Modifier.fillMaxWidth())

        OutlinedTextField(value = description , onValueChange = {description = it},
            label = {Text("Description & amenities")}, enabled = editing , modifier = Modifier.fillMaxWidth())

        OutlinedTextField(value = price , onValueChange = {price = it},
            label = {Text("Price per Night ($)")}, enabled = editing , modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))

        OutlinedTextField(value = maxOcc , onValueChange = {maxOcc = it},
            label = {Text("Maximum occupancy")}, enabled = editing , modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

        OutlinedTextField(value = tags , onValueChange = {tags = it},
            label = {Text("Tags (required, comma separated)")}, enabled = editing , modifier = Modifier.fillMaxWidth())

        OutlinedTextField(value = rules , onValueChange = {rules = it},
            label = {Text("Rules & policies")}, enabled = editing , modifier = Modifier.fillMaxWidth())

        OutlinedTextField(value = latitude , onValueChange = {latitude = it},
            label = {Text("Map Location : latitude")}, enabled = editing , modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))

        OutlinedTextField(value = longitude , onValueChange = {longitude = it},
            label = {Text("Map Location : longitude")}, enabled = editing , modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))

        Spacer(modifier = Modifier.height(16.dp))

        if (errorText.isNotEmpty()){
            Text(errorText , color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!editing) {
                Button(onClick = { editing = true }) {Text("Edit")}
                OutlinedButton(onClick = onDone) {Text("Back") }
            } else {
                Button(
                    enabled = !isSaving,
                    onClick = {
                        isSaving = true
                        val campsite = loaded.copy(
                            name = name.trim(),
                            location = location.trim(),
                            description = description.trim(),
                            maximumOccupancy = maxOcc.toIntOrNull() ?: 0,
                            pricePerNight = price.toDoubleOrNull() ?: 0.0,
                            tags = tags.split(",").map{it.trim() }.filter{it.isNotEmpty()},
                            rulesAndPolicy = rules.trim(),
                            coordinates = run {
                                val latitude = latitude.toDoubleOrNull()
                                val longitude = longitude.toDoubleOrNull()
                                if (latitude != null && longitude != null && latitude in -90.0..90.0 && longitude in -180.0..180.0 ) GeoPoint(latitude, longitude) else null
                            },
                        )
                        if (isNew) {
                            controller.registerCampsite(
                                campsite, ownerId,
                                onSuccess = {Toast.makeText(context, "Campsite registered", Toast.LENGTH_SHORT).show()
                                onDone()
                                },
                                onInvalid = {
                                    errors -> errorText = errors.joinToString("\n"); isSaving = false },
                                onFailure = {errorText = "Could not save campsite."; isSaving = false}
                            )
                                } else {
                            controller.updateCampsite(
                                campsite,
                                onSuccess = {Toast.makeText(context, "Changes saved", Toast.LENGTH_SHORT).show()
                                    loaded = campsite
                                    errorText = ""
                                    editing = false
                                    isSaving = false
                                },
                                onInvalid = {
                                        errors -> errorText = errors.joinToString("\n"); isSaving = false },
                                onFailure = {errorText = "Could not save changes."; isSaving = false}
                            )
                        }
                    }) {
                    Text (if (isNew) "Publish Campsite" else "Update Campsite Details")
        }
            OutlinedButton(onClick = {
                if (isNew) {
                    onDone()
                } else {
                    name = loaded.name
                    location = loaded.location
                    description = loaded.description
                    maxOcc = loaded.maximumOccupancy.toString()
                    price = loaded.pricePerNight.toString()
                    tags = loaded.tags.joinToString(",")
                    rules = loaded.rulesAndPolicy
                    latitude = loaded.coordinates?.latitude?.toString() ?: ""
                    longitude = loaded.coordinates?.longitude?.toString() ?: ""
                    errorText = ""
                    editing = false}
            }) { Text("Cancel")}
            }
        }
    }
}








