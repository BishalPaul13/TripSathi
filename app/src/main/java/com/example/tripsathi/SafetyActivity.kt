package com.example.tripsathi

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Orange = Color(0xFFFF6B00)
private val Bg = Color(0xFFF7F7F7)

class SafetyActivity : ComponentActivity() {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(16.dp)
    ) {

        // 🔙 HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                Icons.Default.ArrowBack,
                contentDescription = null,
                modifier = Modifier.clickable {
                    (context as ComponentActivity).finish()
                }
            )

            Text("Safety Center", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            Spacer(modifier = Modifier.width(24.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 🛡️ STATUS CARD
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Text("Safety Status", fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))

                Text("🟢 Active Protection")
                Text("📍 Tracking: OFF")
                Text("👥 Contacts: Not Set")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 🚨 SOS BUTTON (BIG)
        Button(
            onClick = {
                // later connect SOS
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("🚨 EMERGENCY SOS", color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 🔥 ACTION GRID
        Column {

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                SafetyCard(
                    "Share Location",
                    Icons.Default.LocationOn
                ) {
                    // next step
                }

                SafetyCard(
                    "Safe Zone",
                    Icons.Default.Shield
                ) {
                    // geo-fencing later
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                SafetyCard(
                    "Emergency Contacts",
                    Icons.Default.Call
                ) {
                    context.startActivity(
                        Intent(context, ContactsActivity::class.java)
                    )
                }

                SafetyCard(
                    "Alarm Mode",
                    Icons.Default.Notifications
                ) {
                    // later toggle
                }
            }
        }
    }
}

@Composable
fun RowScope.SafetyCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .weight(1f)
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Icon(icon, contentDescription = null, tint = Orange)

            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}