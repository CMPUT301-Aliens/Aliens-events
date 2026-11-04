package com.example.campsitelotterysystem.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.campsitelotterysystem.controller.CampsiteController
import com.example.campsitelotterysystem.db.FirestoreCampsiteDB
import com.example.campsitelotterysystem.db.getCurrentUserId
import com.example.campsitelotterysystem.model.Campsite
import com.example.campsitelotterysystem.ui.theme.CampsiteLotterySystemTheme



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
    camspiteId : String?,
    onDone: () -> Unit
){
    var mode by remember {mutableStateOf(if (campsiteId == null) "NEW" else "VIEW")
    }
    var campsite by remember { mutableStateOf(Campsite()) }

    LaunchedEffect(campsiteId) {
        if (campsite != null) {
            // TODO: controller.getCampsite(campsiteId,
            //          onSuccess = { found -> if (found != null) campsite = found },
            //          onFailure = { /* show "Couldn't load campsite" */ })
            controller.getCampsite(campsiteId,
                            onSuccess = {found -> if (found != null) campsite = found},
                            onFailure = { "couldn't load campsite"})
        }
    }

    when (mode) {
        "New" -> NewCampsiteForm(controller, ownerId, onDone)
        "View" -> CampsiteDetailsView(campsite, onEdit = {mode = "EDIT"}, onBack = onDone)
        "Edit" -> EditCampsiteForm(
                    controller , campsite,
                    onSaved = {updated -> campsite = updated ; mode = "VIEW"},
                    onCancel = { mode = "VIEW" }
        )
    }
}

@Composable
fun NewCampsiteForm(controller : CampsiteController , ownerId: String, onDone: () -> Unit) {
    val context = LocalContext.current
    // TODO: one state variable per field, e.g.
    //   var name by remember { mutableStateOf("") }
    //   location, description, maxOccupancy, price, latitude, longitude, tags, rules
    var errorText by remember {mutableStateOf("")}

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // TODO: Text("Register Campsite") as a title

        // TODO: one OutlinedTextField per field, e.g.
        //   OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })

        // TODO: if (errorText.isNotEmpty()) Text(errorText)

        // TODO: Button "Publish":
        //   val campsite = Campsite(
        //       name = name, location = location, description = description,
        //       maximumOccupancy = maxOccupancy.toIntOrNull() ?: 0,
        //       pricePerNight = price.toDoubleOrNull() ?: 0.0,
        //       tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        //       rulesAndPolicy = rules
        //   )
        //   controller.registerCampsite(campsite, ownerId,
        //       onSuccess = { Toast.makeText(context, "Campsite registered", Toast.LENGTH_SHORT).show(); onDone() },
        //       onInvalid = { errors -> errorText = errors.joinToString("\n") },
        //       onFailure = { errorText = "Could not save campsite" })

        // TODO: Button "Cancel" → onDone()

    }
}

@Composable
fun CampsiteDetailsView (campsite: Campsite, onEdit: () -> Unit, onBack: () -> ){
    Column(modifier = Modifer.fillMaxWidth().padding(24.dp)){
        // TODO: Text(campsite.name, style = MaterialTheme.typography.headlineMedium)
        // TODO: Text for location, description, "Max guests: ...", "Price: $...", tags, rules

        // TODO: Button "Edit" → onEdit()
        // TODO: Button "Back" → onBack()

    }
}

@Composable
fun EditCamspiteFomr(
    controller : CampsiteController,
    camspite : Campsite,
    onSaved : (Campsite) -> Unit,
    onCancel: () -> Unit
) {
    // TODO: state STARTING from the campsite's values, e.g.
    //   var name by remember { mutableStateOf(campsite.name) }
    //   var maxOccupancy by remember { mutableStateOf(campsite.maximumOccupancy.toString()) }
    var errorText by remember {mutableStateOf("")}

    Column(
        modifer = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
// TODO: the same OutlinedTextFields as NEW

        // TODO: if (errorText.isNotEmpty()) Text(errorText)

        // TODO: Button "Save":
        //   val updated = campsite.copy(name = name, location = location, ...)   ← keeps same id + ownerId
        //   controller.updateCampsite(updated,
        //       onSuccess = { onSaved(updated) },
        //       onInvalid = { errors -> errorText = errors.joinToString("\n") },
        //       onFailure = { errorText = "Could not save changes" })

        // TODO: Button "Cancel" → onCancel()
    }
}