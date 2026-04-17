package com.example.tripsathi

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlin.math.*

private val Orange = Color(0xFFFF6B00)
private val Bg = Color(0xFFF7F7F7)

data class FamousPlace(
    val name: String,
    val category: String,
    val lat: Double,
    val lng: Double,
    val rating: Double
)

class TravelActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TravelScreen { finish() }
        }
    }
}

@Composable
fun TravelScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    userLocation = location
                }
            } catch (e: SecurityException) {
                // Handle permission error
            }
        }
    }

    val famousPlaces = listOf(
        FamousPlace("Taj Mahal", "Historical Landmark", 27.1751, 78.0421, 4.9),
        FamousPlace("Red Fort", "Fortress", 28.6562, 77.2410, 4.6),
        FamousPlace("Qutub Minar", "Victory Tower", 28.5245, 77.1855, 4.5),
        FamousPlace("Gateway of India", "Monument", 18.9220, 72.8347, 4.7),
        FamousPlace("Amber Palace", "Palace", 26.9855, 75.8513, 4.8),
        FamousPlace("Hawa Mahal", "Palace", 26.9239, 75.8267, 4.7),
        FamousPlace("India Gate", "War Memorial", 28.6129, 77.2295, 4.6),
        FamousPlace("Golden Temple", "Religious Site", 31.6200, 74.8765, 4.9),
        FamousPlace("Victoria Memorial", "Museum", 22.5448, 88.3426, 4.7),
        FamousPlace("Lotus Temple", "Religious Site", 28.5535, 77.2588, 4.5),
        FamousPlace("Amer Fort", "Fort", 26.9855, 75.8513, 4.8),
        FamousPlace("Elephanta Caves", "Ancient Caves", 18.9633, 72.9315, 4.4),
        FamousPlace("Ajanta Caves", "Buddhist Rock-cut Caves", 20.5519, 75.7033, 4.8),
        FamousPlace("Charminar", "Mosque/Monument", 17.3616, 78.4747, 4.6)
    )

    val sortedPlaces = userLocation?.let { loc ->
        famousPlaces.sortedBy { place ->
            calculateDistanceInKm(loc.latitude, loc.longitude, place.lat, place.lng)
        }
    } ?: famousPlaces

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            Text(
                text = "Nearby Famous Places",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (userLocation == null && hasPermission) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Orange)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sortedPlaces) { place ->
                    PlaceItem(place, userLocation)
                }
            }
        }
    }
}

@Composable
fun PlaceItem(place: FamousPlace, userLocation: Location?) {
    val distance = userLocation?.let {
        calculateDistanceInKm(it.latitude, it.longitude, place.lat, place.lng)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color(0xFFFFEDE5), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Orange)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = place.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = place.category, color = Color.Gray, fontSize = 12.sp)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = place.rating.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (distance != null) {
                Text(
                    text = "${"%.1f".format(distance)} km",
                    color = Orange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

fun calculateDistanceInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0 // Earth radius in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}
