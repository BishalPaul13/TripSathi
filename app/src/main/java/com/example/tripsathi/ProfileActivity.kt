package com.example.tripsathi

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

private val Orange = Color(0xFFFF6B00)
private val Bg = Color(0xFFF6ECE5)

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfileScreen()
        }
    }
}

@Composable
fun ProfileScreen() {

    val context = LocalContext.current
    val activity = context as ComponentActivity
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseDatabase.getInstance().getReference("users")

    var data by remember { mutableStateOf<Map<String, Any>?>(null) }

    // 🔥 FETCH FIREBASE DATA
    LaunchedEffect(Unit) {
        user?.uid?.let {
            db.child(it).get().addOnSuccessListener {
                data = it.value as? Map<String, Any>
            }
        }
    }

    // 🔥 IMAGE STORAGE
    val prefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE)
    var imageUri by remember { mutableStateOf(prefs.getString("image_uri", null)) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it.toString()
            prefs.edit().putString("image_uri", imageUri).apply()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(16.dp)
    ) {

        // ✅ HEADER (BACK WORKING)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier.clickable {
                    activity.finish()
                }
            )
            Text("Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ✅ PROFILE IMAGE ONLY (SIDE ICONS REMOVED)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Orange)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {

                when {
                    imageUri != null -> {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    user?.photoUrl != null -> {
                        AsyncImage(
                            model = user.photoUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    else -> {
                        Text("U", color = Color.White, fontSize = 24.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // NAME + EMAIL
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                user?.displayName ?: "User",
                fontWeight = FontWeight.Bold
            )

            Text(
                user?.email ?: "",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // YOUR INFO CARD
        ProfileCard(
            title = "Your Info Card",
            icon = Icons.Default.QrCode,
            onClick = {
                context.startActivity(Intent(context, QRActivity::class.java))
            }
        )

        ProfileCard("Help Center", Icons.Default.Info)
        ProfileCard("Settings", Icons.Default.Settings)
        ProfileCard("Language", Icons.Default.Language)

        Spacer(modifier = Modifier.height(16.dp))

        // ABOUT DEVELOPER
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(Icons.Default.Person, contentDescription = null)

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("About Developer", fontWeight = FontWeight.Bold)
                    Text(
                        "Learn about the creator of TripSathi",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Button(
                    onClick = {
                        context.startActivity(
                            Intent(context, DeveloperActivity::class.java)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Orange),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Check it Out", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ProfileCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit = {}
) {

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(icon, contentDescription = null)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(
                    "Explore this section",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Icon(Icons.Default.ArrowForwardIos, contentDescription = null)
        }
    }
}