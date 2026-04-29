package com.example.tripsathi

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.Vibrator
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.tripsathi.ui.theme.TripSathiTheme
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.key
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlin.math.*

data class DangerZone(
    val id: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val radius: Double = 200.0,
    val isApproved: Boolean = true
)

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        setContent {
            TripSathiTheme {
                LocationPermissionWrapper()
            }
        }
    }
}

@Composable
fun LocationPermissionWrapper() {
    val context = LocalContext.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                        permissions[Manifest.permission.SEND_SMS] == true
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS
            )
        )
    }

    if (hasPermission) {
        val fusedLocationClient = remember {
            LocationServices.getFusedLocationProviderClient(context)
        }
        LocationScreen(fusedLocationClient)
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Location and SMS Permissions required")
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun LocationScreen(fusedLocationClient: FusedLocationProviderClient) {

    val context = LocalContext.current
    
    // 🔥 Attempting to get the most reliable DB instance
    val db = remember {
        try {
            FirebaseProvider.database.getReference("danger_zones").also {
                android.util.Log.i("TripSathi", "Firebase DB initialized with explicit URL")
            }
        } catch (e: Exception) {
            android.util.Log.e("TripSathi", "DB Init Error: ${e.message}")
            throw e
        }
    }
    
    val connectedRef = remember { FirebaseProvider.database.getReference(".info/connected") }
    var isFirebaseConnected by remember { mutableStateOf<Boolean?>(null) }

    // 🔥 Monitor Firebase Connection with better diagnostics
    LaunchedEffect(Unit) {
        db.keepSynced(true)
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                isFirebaseConnected = connected
                android.util.Log.d("TripSathi", "Firebase Connection: $connected")
            }
            override fun onCancelled(error: DatabaseError) {
                isFirebaseConnected = false
                android.util.Log.e("TripSathi", "Connection Listener Error: ${error.message}")
            }
        })
    }

    var userLocation by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    val cameraPositionState = rememberCameraPositionState()
    var isMapCentered by remember { mutableStateOf(false) }
    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    
    // 🔥 Confirmation Dialog State
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingLatLng by remember { mutableStateOf<LatLng?>(null) }

    // 🔥 List of danger zones from Firebase
    var dangerZones by remember { mutableStateOf<List<DangerZone>>(emptyList()) }
    // Use rememberUpdatedState to ensure the location callback always has the LATEST list
    val allZonesForCheck by rememberUpdatedState(dangerZones)
    val hasSentAlertState = remember { mutableStateOf(false) }

    // Fetch Danger Zones from Firebase
    LaunchedEffect(Unit) {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val zones = mutableListOf<DangerZone>()
                for (child in snapshot.children) {
                    try {
                        val lat = (child.child("lat").value as? Number)?.toDouble() ?: 0.0
                        val lng = (child.child("lng").value as? Number)?.toDouble() ?: 0.0
                        val radius = (child.child("radius").value as? Number)?.toDouble() ?: 200.0
                        val isApproved = child.child("approved").getValue(Boolean::class.java) ?: true
                        
                        // Show all zones: unapproved as yellow, approved as red.
                        if (lat != 0.0) {
                            zones.add(DangerZone(child.key ?: "", lat, lng, radius, isApproved))
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("TripSathi", "Error parsing zone: ${e.message}")
                    }
                }
                dangerZones = zones

                // An empty snapshot is still a successful Firebase read, so don't show offline.
                if (isFirebaseConnected == null) {
                    isFirebaseConnected = true
                }
            }
            override fun onCancelled(error: DatabaseError) {
                isFirebaseConnected = false
                android.util.Log.e("TripSathi", "Firebase Error: ${error.message}")
                Toast.makeText(context, "Sync Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000
        ).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: result.locations.lastOrNull()

                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    userLocation = currentLatLng

                    if (!isMapCentered) {
                        cameraPositionState.position =
                            CameraPosition.fromLatLngZoom(userLocation, 15f)
                        isMapCentered = true
                    }

                    // ⚠️ CHECK ALL DANGER ZONES (including optimistic ones)
                    var inAnyZone = false
                    val zonesToCheck = allZonesForCheck
                    
                    for (zone in zonesToCheck) {
                        val distance = calculateDistance(
                            location.latitude,
                            location.longitude,
                            zone.lat,
                            zone.lng
                        )
                        
                        // Debugging: Show distance if close
                        if (distance < 500) {
                            android.util.Log.d("TripSathi", "Distance to zone ${zone.id}: ${distance.toInt()}m")
                        }

                        if (distance < zone.radius && zone.isApproved) {
                            inAnyZone = true
                            if (!hasSentAlertState.value) {
                                // Double check if we are still in the zone before firing
                                android.util.Log.w("TripSathi", "SOS TRIGGERED! Inside zone: ${zone.id}")
                                Toast.makeText(context, "⚠️ DANGER DETECTED! Sending SOS...", Toast.LENGTH_LONG).show()
                                sendSOS(context, currentLatLng)
                                hasSentAlertState.value = true
                            }
                            break 
                        }
                    }
                    if (!inAnyZone) {
                        if (hasSentAlertState.value) {
                            android.util.Log.i("TripSathi", "Exited danger zone. Resetting alert state.")
                        }
                        hasSentAlertState.value = false
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    if (showConfirmDialog && pendingLatLng != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Mark as Danger Zone?") },
            text = { Text("Are you sure you want to mark this area as unsafe for all users?") },
            confirmButton = {
                Button(
                    onClick = {
                        val lat = pendingLatLng!!.latitude
                        val lng = pendingLatLng!!.longitude
                        val radius = 200.0
                        
                        val newZoneRef = db.push()
                        val zoneId = newZoneRef.key ?: "temp_${System.currentTimeMillis()}"
                        
                        val newZoneMap = mapOf(
                            "lat" to lat,
                            "lng" to lng,
                            "radius" to radius,
                            "approved" to false
                        )

                        newZoneRef.setValue(newZoneMap).addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "Zone submitted for approval. It will appear after approved=true in Firebase.",
                                Toast.LENGTH_LONG
                            ).show()
                        }.addOnFailureListener {
                            Toast.makeText(context, "Failed to save: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                        
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Yes, Mark it")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(Modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = true,
                mapType = mapType
            ),
            onMapLongClick = { latLng ->
                val tappedZone = dangerZones.firstOrNull { zone ->
                    calculateDistance(latLng.latitude, latLng.longitude, zone.lat, zone.lng) < zone.radius
                }

                if (tappedZone == null) {
                    pendingLatLng = latLng
                    showConfirmDialog = true
                }
            }
        ) {
            Marker(state = MarkerState(position = userLocation))
            
            // 🟡 PREVIEW: Show the area being marked BEFORE saving
            if (showConfirmDialog && pendingLatLng != null) {
                Circle(
                    center = pendingLatLng!!,
                    radius = 200.0,
                    fillColor = Color(0x66FFFF00), // Yellow preview
                    strokeColor = Color.Yellow,
                    strokeWidth = 25f,
                    zIndex = 100f
                )
                Marker(
                    state = MarkerState(position = pendingLatLng!!),
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW),
                    alpha = 0.8f,
                    zIndex = 101f
                )
            }
            
            // 🟡/🔴 Visual Circles and Markers for danger zones
            dangerZones.forEach { zone ->
                key(zone.id) {
                    val zonePos = LatLng(zone.lat, zone.lng)
                    val color = if (zone.isApproved) Color.Red else Color.Yellow
                    val fillColor = if (zone.isApproved) Color(0x55FF0000) else Color(0x55FFFF00)
                    val markerHue = if (zone.isApproved) BitmapDescriptorFactory.HUE_RED else BitmapDescriptorFactory.HUE_YELLOW
                    val title = if (zone.isApproved) "⚠️ DANGER ZONE" else "⚠️ POTENTIAL DANGER"
                    
                    Circle(
                        center = zonePos,
                        radius = zone.radius,
                        fillColor = fillColor,
                        strokeColor = color,
                        strokeWidth = 25f,
                        zIndex = 10f
                    )

                    Marker(
                        state = rememberMarkerState(position = zonePos),
                        title = title,
                        snippet = "Radius: ${zone.radius.toInt()}m${if (!zone.isApproved) " (Pending Approval)" else ""}",
                        icon = BitmapDescriptorFactory.defaultMarker(markerHue),
                        zIndex = 11f
                    )
                }
            }
        }

        // Firebase Connection Status Indicator
        if (isFirebaseConnected == false) {
            Surface(
                color = Color.Red.copy(alpha = 0.8f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Offline - Syncing Disabled",
                        color = Color.White,
                        modifier = Modifier.padding(4.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Satellite Mode Toggle
        SmallFloatingActionButton(
            onClick = {
                mapType = if (mapType == MapType.NORMAL) MapType.SATELLITE else MapType.NORMAL
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .padding(top = 40.dp), // Adjust for potential status bar / header
            containerColor = Color.White,
            contentColor = Color(0xFFFF6B00) // Orange
        ) {
            Icon(
                if (mapType == MapType.NORMAL) Icons.Default.Layers else Icons.Default.Map,
                contentDescription = "Toggle Satellite Mode"
            )
        }

        Button(
            onClick = { sendSOS(context, userLocation) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp) // Space for bottom nav
        ) {
            Text("🚨 SOS")
        }
    }
}

// 📏 Distance function (Haversine formula)
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0                               //Earth radius(in m)
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}

// 🚨 SOS FUNCTION
fun sendSOS(context: Context, location: LatLng) {
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseProvider.database.getReference("users")

    // 🔥 Firebase Alert Log
    FirebaseProvider.database.getReference("alerts").push().setValue(
        mapOf(
            "userId" to user?.uid,
            "lat" to location.latitude,
            "lng" to location.longitude,
            "time" to System.currentTimeMillis()
        )
    ).addOnFailureListener {
        Toast.makeText(context, it.localizedMessage ?: "Failed to send SOS alert", Toast.LENGTH_LONG).show()
    }

    // 🔊 Alarm
    try {
        MediaPlayer.create(context, R.raw.alarm_sound).start()
    } catch (e: Exception) {
        // sound file might be missing
    }

    // 📳 Vibration
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    vibrator.vibrate(3000)

    // Fetch user's emergency contact from database
    if (user != null) {
        db.child(user.uid).child("contact").get().addOnSuccessListener { snapshot ->
            val emergencyNumber = snapshot.value?.toString() ?: ""
            if (emergencyNumber.isNotBlank()) {
                performEmergencyActions(context, location, emergencyNumber)
            } else {
                Toast.makeText(context, "Emergency number not set in profile!", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to fetch emergency contact", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
    }
}

fun performEmergencyActions(context: Context, location: LatLng, emergencyNumber: String) {
    // 📩 SMS
    try {
        val msg = "🚨 I am in danger! My live location: https://maps.google.com/?q=${location.latitude},${location.longitude}"
        
        // Use a more robust way to get SmsManager
        val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }

        // Use sendMultipartTextMessage because the emoji and URL might exceed character limits for a single SMS
        val parts = smsManager.divideMessage(msg)
        smsManager.sendMultipartTextMessage(emergencyNumber, null, parts, null, null)

        Toast.makeText(context, "Emergency SMS sent to $emergencyNumber", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "SMS failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }

    // 📞 Call
    val intent = Intent(Intent.ACTION_DIAL)
    intent.data = Uri.parse("tel:112")
    context.startActivity(intent)

    Toast.makeText(context, "🚨 SOS Initiated!", Toast.LENGTH_SHORT).show()
}
