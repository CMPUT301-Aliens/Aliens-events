package com.example.campsitelotterysystem.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.campsitelotterysystem.controller.CampsiteController
import com.example.campsitelotterysystem.db.FirestoreCampsiteDB
import com.example.campsitelotterysystem.db.getCurrentUserId
import com.example.campsitelotterysystem.model.Campsite
import com.example.campsitelotterysystem.ui.theme.CampsiteLotterySystemTheme


class OwnerDashboardActivity : ComponentActivity() {
    private var refreshKey by mutableIntStateOf(0)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val controller = CampsiteController(FirestoreCampsiteDB())
        val ownerId = getCurrentUserId(this)

        setContent {
            CampsiteLotterySystemTheme {
                OwnerDashboardScreen(controller, ownerId, refreshKey)
            }
        }
    }
    override fun onResume(){
        super.onResume()
        refreshKey++
    }
}

@Composable
fun OwnerDashboardScreen(controller: CampsiteController, ownerId: String, refreshKey : Int) {
    val context = LocalContext.current
    var campsites by remember { mutableStateOf<List<Campsite>>(emptyList()) }
    var errorMessages by remember {mutableStateOf("") }

    LaunchedEffect(refreshKey) {
        controller.getOwnerCampsites(ownerId,
            onSuccess = {list -> campsites = list },
            onFailure = {errorMessages = "couldn't display campsites"
            })

    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            text = "My Campsites",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val intent = Intent(context, CampsiteEditActivity::class.java)
            context.startActivity(intent)
        }) {
            Text(text = "Register Campsite")
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (errorMessages.isNotEmpty()){
            Text(errorMessages)
        }


        if (campsites.isEmpty()) {
            Text ("you haven't registered any campsites yet ")
        }else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ){
                items(campsites, key = {campsite -> campsite.id}) { campsite ->
                    Card(
                        onClick = {
                            val intent = Intent(context, CampsiteEditActivity::class.java)
                            intent.putExtra(CampsiteEditActivity.EXTRA_CAMPSITE_ID, campsite.id)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ){
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ){
                            Text(
                                text = campsite.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = campsite.location,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Max guests : ${campsite.maximumOccupancy}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (campsite.published) "Published" else "Not published",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Button(onClick = {
                                val intent = Intent(context, LotteryEditActivity::class.java)
                                intent.putExtra(LotteryEditActivity.EXTRA_CAMPSITE_ID, campsite.id)
                                context.startActivity(intent)
                            }){
                                Text("Manage Lottery")
                            }
                        }
                    }
                }
            }
        }
    }
}
