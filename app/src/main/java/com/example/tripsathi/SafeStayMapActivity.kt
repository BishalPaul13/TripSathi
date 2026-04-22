package com.example.tripsathi

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

private val Orange = Color(0xFFFF6B00)
private val Bg = Color(0xFFF7F7F7)

data class SafeStay(
    val name: String,
    val rating: Double,
    val lat: Double,
    val lng: Double,
    val reviews: Int
)

class SafeStayMapActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SafeStayMapScreen { finish() }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun SafeStayMapScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = FirebaseProvider.database.getReference("safe_stays")
    
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(20.5937, 78.9629), 5f)
    }

    // Demo Stays
    val stays = listOf(
        SafeStay("Hotel Safe Residency", 4.8, 28.6139, 77.2090, 124),
        SafeStay("Tourist Haven", 4.5, 19.0760, 72.8777, 89),
        SafeStay("Guardian Suites", 4.9, 12.9716, 77.5946, 210)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            Text("Safe-Stay Ratings", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Box(modifier = Modifier.weight(1f)) {
            var mapType by remember { mutableStateOf(MapType.NORMAL) }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission,
                    mapType = mapType
                )
            ) {
                stays.forEach { stay ->
                    Marker(
                        state = MarkerState(position = LatLng(stay.lat, stay.lng)),
                        title = stay.name,
                        snippet = "Rating: ${stay.rating}/5.0 (${stay.reviews} reviews)",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                    )
                }
            }

            // Satellite Mode Toggle
            SmallFloatingActionButton(
                onClick = {
                    mapType = if (mapType == MapType.NORMAL) MapType.SATELLITE else MapType.NORMAL
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                containerColor = Color.White,
                contentColor = Orange
            ) {
                Icon(
                    if (mapType == MapType.NORMAL) Icons.Default.Layers else Icons.Default.Map,
                    contentDescription = "Toggle Satellite Mode"
                )
            }

            // Floating Rating Card (Overlay)
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Rate Current Area Safety", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(5) { index ->
                            IconButton(onClick = {
                                Toast.makeText(context, "Thank you for rating!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300))
                            }
                        }
                    }
                }
            }
        }
    }
}
