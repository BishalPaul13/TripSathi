package com.example.tripsathi

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Orange = Color(0xFFFF6B00)
private val Bg = Color(0xFFF7F7F7)
private val UserBubble = Color(0xFFFFEDE5)
private val BotBubble = Color.White

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

class SafePathChatActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SafePathChatScreen { finish() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafePathChatScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    
    var messages by remember { 
        mutableStateOf(listOf(ChatMessage("Hello! I am your Safe Path Assistant. How can I help you today?", false))) 
    }
    
    var currentStep by remember { mutableStateOf(0) }
    var showOptions by remember { mutableStateOf(true) }
    var showPath by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var mapType by remember { mutableStateOf(MapType.NORMAL) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(20.5937, 78.9629), 5f)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val pos = LatLng(it.latitude, it.longitude)
                        userLocation = pos
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(pos, 15f)
                    }
                }
            } catch (e: SecurityException) { }
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Demo Path Data (Calculated relative to user's real location)
    val safePath = remember(userLocation) {
        userLocation?.let { loc ->
            listOf(
                loc,
                LatLng(loc.latitude + 0.002, loc.longitude + 0.001),
                LatLng(loc.latitude + 0.004, loc.longitude + 0.003),
                LatLng(loc.latitude + 0.006, loc.longitude + 0.002)
            )
        } ?: emptyList()
    }

    val options = listOf(
        listOf("Find a safe route", "I feel unsafe here"),
        listOf("Yes, show well-lit paths", "No, show shortest path"),
        listOf("I'm satisfied, thanks!", "I still need help")
    )

    LaunchedEffect(messages.size) {
        listState.animateScrollToItem(messages.size - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        // Header
        TopAppBar(
            title = { Text("Safe Path Assistant", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        // Map Section (Shows when path is requested)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (showPath) 250.dp else 0.dp)
        ) {
            if (showPath && userLocation != null) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = true,
                        mapType = mapType
                    )
                ) {
                    Polyline(
                        points = safePath,
                        color = Color(0xFF4CAF50),
                        width = 12f
                    )
                    Marker(state = MarkerState(position = safePath.first()), title = "Your Location")
                    Marker(state = MarkerState(position = safePath.last()), title = "Safe Destination")
                }

                // Satellite Mode Toggle
                SmallFloatingActionButton(
                    onClick = {
                        mapType = if (mapType == MapType.NORMAL) MapType.SATELLITE else MapType.NORMAL
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    containerColor = Color.White,
                    contentColor = Orange
                ) {
                    Icon(
                        if (mapType == MapType.NORMAL) Icons.Default.Layers else Icons.Default.Map,
                        contentDescription = "Toggle Satellite Mode"
                    )
                }
            }
        }

        // Chat Area
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg)
            }
        }

        // Options Area
        if (showOptions && currentStep < options.size) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options[currentStep].forEach { option ->
                    Button(
                        onClick = {
                            scope.launch {
                                messages = messages + ChatMessage(option, true)
                                showOptions = false
                                delay(1000)
                                handleBotResponse(option) { botMsg ->
                                    messages = messages + ChatMessage(botMsg, false)
                                    if (option.contains("Yes") || option.contains("route")) {
                                        showPath = true
                                    }
                                    currentStep++
                                    showOptions = true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = if (option.contains("unsafe") || option.contains("help")) Color.Red else Orange),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(option, color = Color.White)
                    }
                }
            }
        } else if (currentStep >= options.size || !showOptions) {
            // Assistance / Call 112 Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Need further assistance?", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL)
                        intent.data = Uri.parse("tel:112")
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Call Emergency 112", color = Color.White, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onBack) {
                    Text("Close Chat", color = Orange)
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            color = if (message.isUser) UserBubble else BotBubble,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 0.dp,
                bottomEnd = if (message.isUser) 0.dp else 16.dp
            ),
            shadowElevation = 1.dp
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                fontSize = 15.sp,
                color = Color.Black
            )
        }
    }
}

fun handleBotResponse(userText: String, onResponse: (String) -> Unit) {
    val response = when {
        userText.contains("route") -> "I'm analyzing the safest routes. Would you like to see well-lit paths?"
        userText.contains("unsafe") -> "I'm sorry to hear that. Please stay in a public area. Should I guide you to the nearest police station or well-lit path?"
        userText.contains("Yes") -> "Great choice. I've highlighted the well-lit paths on your map. Are you satisfied with the result?"
        userText.contains("No") -> "Okay, showing the fastest route instead. Be careful. Are you satisfied?"
        userText.contains("satisfied") -> "Glad I could help! Stay safe."
        userText.contains("help") -> "I recommend contacting emergency services for immediate assistance."
        else -> "I understand. How else can I assist you?"
    }
    onResponse(response)
}
