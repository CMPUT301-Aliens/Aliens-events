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

/**
 * CampsiteEditActivity: the campsite form (mockup 13). One screen, 3 modes:
 * NEW (#3, #4) | VIEW (#6) | EDIT (#5)
 *
 * FLOW: No campsiteId → NEW | campsiteId → load via Controller → VIEW → Edit → EDIT
 *       Publish/Save → build Campsite → Controller → error? show it : success? confirm + go back
 *
 * TODO
 * - Fields (#4): name, location, description, max occupancy, price, lat + long,
 *   tags, rules, available dates, image (optional, do LAST)
 * - Buttons: Publish (NEW) | Edit (VIEW) | Save + Cancel (EDIT) | Back (VIEW → dashboard)
 * - Show controller errors, keep the form open
 * - Success: "Campsite registered" → back to dashboard | "Changes saved" → VIEW with new values
 * - VIEW (#6): show all details clearly
 *
 * NOT MINE: Join button (#28), entrant view (#24), QR, reservations, lottery screens
 */

class CampsiteEditActivity : ComponentActivity(){
// Opened from the dashboard:
//   - no campsite ID passed → NEW mode
//   - campsite ID passed   → load it with controller.getCampsite(id) → VIEW mode

// NewCampsiteForm: empty fields + Publish
//   Publish → build Campsite → controller.registerCampsite
//   → onInvalid: show errors | onSuccess: "Campsite registered" + finish() back to dashboard

// CampsiteDetailsView: campsite info as text + Edit button
//   Edit → switch to EDIT mode | Back → finish() back to dashboard

// EditCampsiteForm: same fields, prefilled + Save + Cancel
//   Save → controller.updateCampsite → onSuccess: "Changes saved" → back to VIEW
//