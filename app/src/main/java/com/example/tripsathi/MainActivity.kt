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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.google.firebase.database.FirebaseDatabase
import kotlin.math.*

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

    var userLocation by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    val cameraPositionState = rememberCameraPositionState()
    var isMapCentered by remember { mutableStateOf(false) }

    // ⚠️ Danger Zone (demo)
    val dangerZone = LatLng(28.4595, 77.0266)
    val dangerRadius = 200.0

    LaunchedEffect(Unit) {

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000
        ).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: result.locations.lastOrNull()

                if (location != null) {
                    userLocation = LatLng(location.latitude, location.longitude)

                    if (!isMapCentered) {
                        cameraPositionState.position =
                            CameraPosition.fromLatLngZoom(userLocation, 15f)
                        isMapCentered = true
                    }

                    // ⚠️ GEO-FENCING CHECK
                    val distance = calculateDistance(
                        location.latitude,
                        location.longitude,
                        dangerZone.latitude,
                        dangerZone.longitude
                    )

                    if (distance < dangerRadius) {
                        Toast.makeText(context, "⚠️ Danger Zone!", Toast.LENGTH_LONG).show()

                        val vibrator =
                            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        vibrator.vibrate(2000)
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

    Box(Modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            Marker(state = MarkerState(position = userLocation))
        }

        Button(
            onClick = { sendSOS(context, userLocation) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text("🚨 SOS")
        }
    }
}

// 📏 Distance function
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0
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
    val db = FirebaseDatabase.getInstance().getReference("users")

    // 🔥 Firebase Alert Log
    FirebaseDatabase.getInstance().getReference("alerts").push().setValue(
        mapOf(
            "userId" to user?.uid,
            "lat" to location.latitude,
            "lng" to location.longitude,
            "time" to System.currentTimeMillis()
        )
    )

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
