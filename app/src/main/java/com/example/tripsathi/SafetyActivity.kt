package com.example.tripsathi

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth

private val Orange = Color(0xFFFF6B00)
private val Bg = Color(0xFFF7F7F7)
private val DangerRed = Color(0xFFE53935)

class SafetyActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SafetyScreen()
        }
    }
}

@Composable
fun SafetyScreen() {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseProvider.database.getReference("users")

    var emergencyContact by remember { mutableStateOf("Not Set") }
    var userLocation by remember { mutableStateOf<Location?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (locationGranted) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    userLocation = location
                }
            } catch (e: SecurityException) { }
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS
            )
        )
        
        user?.uid?.let { uid ->
            db.child(uid).child("contact").get().addOnSuccessListener { snapshot ->
                emergencyContact = snapshot.value?.toString() ?: "Not Set"
            }.addOnFailureListener { error ->
                Toast.makeText(context, error.localizedMessage ?: "Failed to load emergency contact", Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 🔙 HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { activity.finish() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            Text(
                text = stringResource(id = R.string.safety),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp)) // Balance header
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🛡️ PREMIUM STATUS CARD
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF4CAF50))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Active Protection",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                StatusRow(Icons.Default.MyLocation, "Live Tracking", if (userLocation != null) "ENABLED" else "FETCHING...")
                StatusRow(Icons.Default.Phone, "Emergency Contact", emergencyContact)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 🚨 BIG SOS BUTTON
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .size(150.dp)
                    .clickable {
                        if (userLocation != null) {
                            sendSOS(context, LatLng(userLocation!!.latitude, userLocation!!.longitude))
                        } else {
                            Toast.makeText(context, "Fetching location, please wait...", Toast.LENGTH_SHORT).show()
                        }
                    },
                shape = CircleShape,
                color = DangerRed,
                shadowElevation = 8.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Emergency,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "SOS",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp
                    )
                }
            }
        }

        Text(
            text = "Press in case of emergency",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 🔥 RELEVANT SAFETY CARDS
        Text(
            text = "Safety Tools",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SafetyToolCard(
                title = "Safe Path",
                subtitle = "AI-suggested routes",
                icon = Icons.Default.Map,
                modifier = Modifier.weight(1f)
            ) {
                context.startActivity(Intent(context, SafePathChatActivity::class.java))
            }
            SafetyToolCard(
                title = "Fake Call",
                subtitle = "Discreet escape",
                icon = Icons.Default.Call,
                modifier = Modifier.weight(1f)
            ) { /* Future */ }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SafetyToolCard(
                title = "Contacts",
                subtitle = "Manage guardians",
                icon = Icons.Default.Group,
                modifier = Modifier.weight(1f)
            ) {
                context.startActivity(Intent(context, UserInfoActivity::class.java))
            }
            SafetyToolCard(
                title = "Siren",
                subtitle = "Attract attention",
                icon = Icons.Default.Campaign,
                modifier = Modifier.weight(1f)
            ) { /* Future */ }
        }
    }
}

@Composable
fun StatusRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "$label: ", fontSize = 14.sp, color = Color.Gray)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
    }
}

@Composable
fun SafetyToolCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = null, tint = Orange)
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = subtitle, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}
