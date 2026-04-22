package com.example.tripsathi

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth

private val Orange = Color(0xFFFF6B00)
private val Bg = Color(0xFFF6ECE5)

class ProfileActivity : BaseActivity() {

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
    val db = FirebaseProvider.database.getReference("users")

    var data by remember { mutableStateOf<Map<String, Any>?>(null) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    // 🔥 FETCH FIREBASE DATA
    LaunchedEffect(Unit) {
        user?.uid?.let {
            db.child(it).get().addOnSuccessListener {
                data = it.value as? Map<String, Any>
            }.addOnFailureListener { error ->
                Toast.makeText(context, error.localizedMessage ?: "Failed to load profile", Toast.LENGTH_LONG).show()
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

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { langCode ->
                val settingsPrefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
                settingsPrefs.edit().putString("My_Lang", langCode).apply()
                showLanguageDialog = false
                
                // Global Refresh: Restart the app from the dashboard
                val intent = Intent(context, DashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        // ✅ HEADER
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
            Text(stringResource(id = R.string.profile), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ✅ PROFILE IMAGE
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
                        Text(user?.displayName?.firstOrNull()?.toString() ?: "U", color = Color.White, fontSize = 24.sp)
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

        // PROFILE CARDS
        ProfileCard(
            title = stringResource(id = R.string.your_info_card),
            icon = Icons.Default.QrCode,
            onClick = {
                context.startActivity(Intent(context, QRActivity::class.java))
            }
        )

        ProfileCard(stringResource(id = R.string.help_center), Icons.Default.Info)
        ProfileCard(stringResource(id = R.string.settings), Icons.Default.Settings)
        ProfileCard(
            title = stringResource(id = R.string.language),
            icon = Icons.Default.Language,
            onClick = { showLanguageDialog = true }
        )

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
                    Text(stringResource(id = R.string.about_developer), fontWeight = FontWeight.Bold)
                    Text(
                        stringResource(id = R.string.learn_about_creator),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Button(
                    onClick = {
                        context.startActivity(Intent(context, DeveloperActivity::class.java))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Orange),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(stringResource(id = R.string.check_it_out), color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun LanguageSelectionDialog(onDismiss: () -> Unit, onLanguageSelected: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.select_language)) },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLanguageSelected("en") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(id = R.string.english))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLanguageSelected("hi") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(id = R.string.hindi))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLanguageSelected("te") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(id = R.string.telugu))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
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
                    stringResource(id = R.string.explore_section),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Icon(Icons.Default.ArrowForwardIos, contentDescription = null)
        }
    }
}
