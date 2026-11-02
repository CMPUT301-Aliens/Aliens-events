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

class LotteryEditActivity : ComponentActivity(){
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
                    Text(
                        text = "Lottery for campsite: $campsiteId",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

}

