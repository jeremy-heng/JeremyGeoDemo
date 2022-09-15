package com.example.jeremycomposegeopubnub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ExperimentalGraphicsApi
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.jeremycomposegeopubnub.ui.theme.JeremyComposeGeoPubNubTheme
import com.pubnub.api.PNConfiguration
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
import com.pubnub.api.models.consumer.pubsub.PNSignalResult
import com.pubnub.api.models.consumer.pubsub.files.PNFileEventResult


class MainActivity : ComponentActivity() {
    lateinit var locationManager: LocationManager
    var receivedMessage = mutableStateOf("default")
    var receivedList = mutableStateListOf<ReceivedMessage>()
    var isMessage = mutableStateOf(true)


    val config = PNConfiguration(uuid = "whatever").apply{
        //My sub keys
        subscribeKey = "YOUR SUB KEY HERE"
        publishKey = "YOUR PUB KEY HERE"
        uuid = "myUniqueUUID"
    }

    val pubnub = PubNub(config)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationManager = LocationManager(this)
        locationManager.startLocationUpdates()
        pubnub.subscribe(channels = listOf("geodata"),
        withPresence = true)

       //pubnub.configuration.filterExpression = "name LIKE 'Zaba*'"


        pubnub.addListener(object: SubscribeCallback() {
            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                super.message(pubnub, pnMessageResult)
                //receivedMessage.value = pnMessageResult.message.toString()
                receivedList.add(ReceivedMessage(pnMessageResult.message.toString(), true))

            }

            override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
                super.presence(pubnub, pnPresenceEventResult)
                receivedList.add(ReceivedMessage(pnPresenceEventResult.event.toString(),true))


            }


            override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {
                super.signal(pubnub, pnSignalResult)
                receivedList.add(ReceivedMessage(pnSignalResult.message.toString(),false))
            }
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                print("status")
            }

            override fun file(pubnub: PubNub, pnFileEventResult: PNFileEventResult) {
                super.file(pubnub, pnFileEventResult)
                receivedList.add(ReceivedMessage(pnFileEventResult.message.toString(),false))
            }
        })

        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "publishscreen") {
                composable("publishscreen") {
                    JeremyComposeGeoPubNubTheme {

                        // A surface container using the 'background' color from the theme
                        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                            Greeting(locationManager.myLocation.value, receivedMessage.value, isMessage.value, onStateChange = {newState: Boolean -> isMessage.value = newState }, navController)

                        }

                    }
                }
                composable("subscribescreen") {
                    JeremyComposeGeoPubNubTheme {

                        // A surface container using the 'background' color from the theme
                        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                            SubscribeScreen(receivedMessage.value,  navController, receivedList)

                        }

                    }
                }
            }




        }

        //setContentView(R.layout.layout)
    }

    override fun onResume() {
        super.onResume()
        locationManager.startLocationUpdates()
    }


}



@Composable
fun Greeting(name: String,
             received: String,
             isMessage: Boolean,
             onStateChange: (Boolean) -> Unit,
            navController: NavController) {
    Column {
        //var state by remember { mutableStateOf(isMessage) }
        Text (text="Current Location:", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(text = "$name")
        Divider()
        Column(Modifier.selectableGroup()) {
            Text("Send as", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = isMessage,
                    onClick = { onStateChange(true) })
                Text(text = "Send as Message", fontSize = 16.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = !isMessage,
                    onClick = { onStateChange(false) })
                Text(text = "Send as Signal", fontSize = 16.sp)
            }

        }
        Divider()
        Column (
            modifier = Modifier.fillMaxHeight().fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally)
        {
            Button(onClick = { navController.navigate("subscribescreen") }) {
                Text(text = "To Subscribe Screen")
        }

        }


    }
    

}

@OptIn(ExperimentalGraphicsApi::class)
@Composable
fun SubscribeScreen(
             received: String,
             navController: NavController,
                receivedList: MutableList<ReceivedMessage>) {

    Column {
        LazyColumn {
                items(receivedList) { index ->
                    if(index.isMessage) {
                        Text(text = "Message: ${index.message}", color = Color.Black)
                    } else {
                        Text(text = "Signal: ${index.message}", color = Color.hsv(114F, 0.68F, 0.47F))
                    }
                }
        }
        Column(modifier = Modifier. fillMaxWidth().fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { navController.navigate("publishscreen") }) {
                Text(text = "To Publish Screen")
            }
        }

    }


}

class ReceivedMessage(
    val message: String,
    val isMessage: Boolean
)

@Preview(showBackground = false, backgroundColor = 234)
@Composable
fun DefaultPreview() {
    JeremyComposeGeoPubNubTheme {
        //Greeting("Jeremy2", "Jeremy3", true, onStateChange = (true))
    }
}