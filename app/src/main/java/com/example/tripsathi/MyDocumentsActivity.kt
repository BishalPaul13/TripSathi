package com.example.tripsathi

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream

private val Orange = Color(0xFFFF6B00)
private val Bg = Color(0xFFF7F7F7)

class MyDocumentsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyDocumentsScreen { finish() }
        }
    }
}

@Composable
fun MyDocumentsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseDatabase.getInstance().getReference("user_docs")

    var adhaarNumber by remember { mutableStateOf("") }
    var panNumber by remember { mutableStateOf("") }
    var adhaarImageUri by remember { mutableStateOf<Uri?>(null) }
    var panImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch numbers from Firebase
    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            db.child(uid).get().addOnSuccessListener { snapshot ->
                val data = snapshot.value as? Map<String, Any>
                if (data != null) {
                    adhaarNumber = data["adhaar"]?.toString() ?: ""
                    panNumber = data["pan"]?.toString() ?: ""
                }
                isLoading = false
            }
        }
    }

    // Load images locally
    LaunchedEffect(Unit) {
        val adhaarFile = File(context.filesDir, "adhaar_${user?.uid}.jpg")
        if (adhaarFile.exists()) adhaarImageUri = Uri.fromFile(adhaarFile)
        
        val panFile = File(context.filesDir, "pan_${user?.uid}.jpg")
        if (panFile.exists()) panImageUri = Uri.fromFile(panFile)
    }

    val adhaarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            saveImageLocally(context, it, "adhaar_${user?.uid}.jpg")
            adhaarImageUri = Uri.fromFile(File(context.filesDir, "adhaar_${user?.uid}.jpg"))
        }
    }

    val panLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            saveImageLocally(context, it, "pan_${user?.uid}.jpg")
            panImageUri = Uri.fromFile(File(context.filesDir, "pan_${user?.uid}.jpg"))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            Text("My Documents", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Orange)
            }
        } else {
            // Adhaar Card
            DocCard(
                title = "Aadhaar Card",
                number = adhaarNumber,
                onNumberChange = { adhaarNumber = it },
                imageUri = adhaarImageUri,
                onImageClick = { adhaarLauncher.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // PAN Card
            DocCard(
                title = "PAN Card",
                number = panNumber,
                onNumberChange = { panNumber = it },
                imageUri = panImageUri,
                onImageClick = { panLauncher.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    user?.uid?.let { uid ->
                        val data = mapOf("adhaar" to adhaarNumber, "pan" to panNumber)
                        db.child(uid).setValue(data).addOnSuccessListener {
                            Toast.makeText(context, "Document numbers saved!", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange)
            ) {
                Text("Save Numbers to Cloud", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DocCard(
    title: String,
    number: String,
    onNumberChange: (String) -> Unit,
    imageUri: Uri?,
    onImageClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = Orange)
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = number,
                onValueChange = onNumberChange,
                label = { Text("Document Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0F0F0))
                    .clickable { onImageClick() },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.Gray)
                        Text("Add Document Image", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

fun saveImageLocally(context: Context, uri: Uri, fileName: String) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
