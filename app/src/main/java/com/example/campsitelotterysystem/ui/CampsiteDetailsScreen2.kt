package com.example.campsitelotterysystem.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.campsitelotterysystem.model.Campsite
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.campsitelotterysystem.db.LotteryDB
import com.example.campsitelotterysystem.model.Lottery
import com.example.campsitelotterysystem.model.User
import java.util.Date
import java.util.concurrent.TimeUnit




//cite claude
// have kotlin date() object. how to get string representation of how many days/hours/minutes date is in future
fun getTimeUntilLotteryEnd(endDate: Date?) : String {
    if (endDate == null) {return "Error!"}
    val diffMs = endDate.time - System.currentTimeMillis()
    if (diffMs <= 0) return "in the past"

    val days = TimeUnit.MILLISECONDS.toDays(diffMs)
    val hours = TimeUnit.MILLISECONDS.toHours(diffMs) % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs) % 60

    if (days.toInt() != 0) {
        return (days.toString() + " Day(s)")
    }
    if (hours.toInt() != 0) {
        return (hours.toString() + " Hour(s)")
    }
    if (minutes.toInt() > 0) {
        return (minutes.toString() + " Minute(s)")
    }
    else (return "<1 Minute!!")

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampsiteDetailsScreen2(
    user : User,
    campsite: Campsite,
    lotteryDB: LotteryDB,
    onBack: () -> Unit
) {



    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Lottery Information")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            var lottery by remember {mutableStateOf<Lottery?>(null)}
            var errorMessage by remember {mutableStateOf<String>("")}
            var buttonError by remember {mutableStateOf<Boolean>(false)} //set to true if any error occurs such that lottery loads but button cannot determine correct state
            var isEnrolled by remember {mutableStateOf<Boolean>(false)}

            var bannerText by remember {mutableStateOf<String?>(null)}
            var isBannerGreen by remember { mutableStateOf<Boolean>(true) }

            fun updateBanner(text : String, bannerIsGreen : Boolean) {
                bannerText = text
                isBannerGreen = bannerIsGreen

            }

            LaunchedEffect(campsite.id) {
                lotteryDB.getLotteryForCampsite(
                    campsiteId = campsite.id,
                    onSuccess = { campsiteLottery -> lottery = campsiteLottery },
                    onFailure = { errorMessage = "Could not load lottery. Please go back"
                    }
                )
                if (errorMessage == "") {
                    lotteryDB.getLotteriesWithUserInWaitlist(
                        userId = user.deviceId,
                        onSuccess = {usersLotteries -> isEnrolled = (lottery in usersLotteries)},
                        onFailure = { buttonError = true})}

                }




            if (lottery == null) {
                Text(errorMessage)
            }
            else {

                bannerText?.let {
                    Text(
                        it,
                        color = if (isBannerGreen) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                }



                //display info

                Text("Lottery Information:")
                Text("Registration period - ${getTimeUntilLotteryEnd(lottery?.registrationEndDate)}")
                Text("Current Waitlist -  ${lottery?.waitingList?.size ?: "Error!"}")






                //join button
                if (!buttonError and !isEnrolled) {



                    Button(onClick = {
                        //attempt to join lottery
                        var joinedLottery : Lottery
                        if (user.deviceId !in lottery!!.waitingList) {
                            joinedLottery = lottery!!.copy(waitingList = lottery!!.waitingList + user.deviceId)
                        }
                        else { //if user is somehow already enrolled, it is easiest to proceed as if they were enrolling, but ensure that they aren't listed twice
                            joinedLottery = lottery!!.copy()}
                        lotteryDB.updateLottery(joinedLottery, {
                            isEnrolled = true
                            lottery = joinedLottery
                            //display banner

                        },{
                            buttonError = true
                            //error banner or smth
                        })

                    }) {
                        Text("Join Lottery")
                    }}

                //LEAVE BUTTON
                if (!buttonError and isEnrolled) {


                    Button(onClick = {
                        //attempt to leave lottery
                        var leftLottery : Lottery
                        if (user.deviceId !in lottery!!.waitingList) {
                            leftLottery = lottery!!.copy(waitingList = lottery!!.waitingList + user.deviceId)
                        }
                        else {
                            leftLottery = lottery!!.copy()}
                        lotteryDB.updateLottery(leftLottery, {
                            isEnrolled = false
                            lottery = leftLottery
                            //display banner

                        },{
                            buttonError = true
                            //error banner or smth
                        })



                }) {
                        Text("Leave Lottery")
                    }}
                if (buttonError) {
                    Text("Sorry, an error occurred regarding your registration status. Please try again.")
                }





            }


           
        }
    }
}

