package com.example.tripsathi

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

class HospitalMapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HospitalMapScreen() }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun HospitalMapScreen() {

    val context = androidx.compose.ui.platform.LocalContext.current
    val fused = LocationServices.getFusedLocationProviderClient(context)

    var userLocation by remember { mutableStateOf(LatLng(0.0, 0.0)) }

    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(Unit) {

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 3000
        ).build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation
                if (loc != null) {
                    userLocation = LatLng(loc.latitude, loc.longitude)
                    cameraPositionState.position =
                        CameraPosition.fromLatLngZoom(userLocation, 14f)
                }
            }
        }

        fused.requestLocationUpdates(request, callback, android.os.Looper.getMainLooper())
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {

        Marker(
            state = MarkerState(position = userLocation),
            title = "You"
        )

        // 🔥 DEMO HOSPITAL MARKERS
        Marker(
            state = MarkerState(position = LatLng(userLocation.latitude + 0.01, userLocation.longitude)),
            title = "Nearby Hospital"
        )
    }
}