package com.example.tripsathi

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
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
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

private val Orange = Color(0xFFFF6B00)
private val Bg = Color(0xFFF7F7F7)

class RecentActivityActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecentActivityScreen {
                finish()
            }
        }
    }
}

@Composable
fun RecentActivityScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseProvider.database.getReference("alerts")
    var alerts by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(user?.uid) {
        if (user != null) {
            // Remove the query constraint temporarily to see if data exists at all, 
            // or ensure 'userId' index is created in Firebase rules.
            // For now, let's fetch all and filter manually to debug if 'userId' is the issue.
            db.get().addOnSuccessListener { snapshot ->
                val list = mutableListOf<Map<String, Any>>()
                snapshot.children.forEach { child ->
                    val data = child.value as? Map<String, Any>
                    if (data != null && data["userId"] == user.uid) {
                        list.add(data)
                    }
                }
                alerts = list.sortedByDescending { it["time"] as? Long ?: 0L }
                isLoading = false
            }.addOnFailureListener {
                isLoading = false
                Toast.makeText(context, it.localizedMessage ?: "Failed to load recent activity", Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            Text(
                text = stringResource(id = R.string.recent_activity),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Orange)
            }
        } else if (alerts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "No recent activity found", color = Color.Gray)
                    Text(text = "Try triggering an SOS first", fontSize = 12.sp, color = Color.LightGray)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(alerts) { alert ->
                    ActivityItem(alert)
                }
            }
        }
    }
}

@Composable
fun ActivityItem(alert: Map<String, Any>) {
    val time = alert["time"] as? Long ?: 0L
    val date = Date(time)
    val format = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val dateString = format.format(date)

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFFFEDE5), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Orange, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = "Emergency SOS Triggered", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = dateString, color = Color.Gray, fontSize = 12.sp)
                val lat = alert["lat"]
                val lng = alert["lng"]
                if (lat != null && lng != null) {
                    Text(text = "Location: $lat, $lng", color = Orange, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
