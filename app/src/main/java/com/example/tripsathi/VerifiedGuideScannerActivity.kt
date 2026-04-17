package com.example.tripsathi

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

private val Orange = Color(0xFFFF6B00)
private val Bg = Color(0xFFF7F7F7)

class VerifiedGuideScannerActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GuideScannerScreen { finish() }
        }
    }
}

@Composable
fun GuideScannerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var scanResult by remember { mutableStateOf<String?>(null) }
    var guideData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isChecking by remember { mutableStateOf(false) }

    val barcodeLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            scanResult = result.contents
            isChecking = true
            // Check Firebase
            FirebaseDatabase.getInstance().getReference("verified_guides")
                .child(result.contents)
                .get()
                .addOnSuccessListener { snapshot ->
                    guideData = snapshot.value as? Map<String, Any>
                    isChecking = false
                }
                .addOnFailureListener {
                    isChecking = false
                    Toast.makeText(context, "Verification failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val options = ScanOptions()
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            options.setPrompt("Scan Guide/Driver QR Code")
            options.setBeepEnabled(true)
            options.setOrientationLocked(false)
            barcodeLauncher.launch(options)
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            Text("Verify Guide/Driver", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(40.dp))

        if (scanResult == null) {
            Icon(
                Icons.Default.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = Orange
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("Verify your guide or driver instantly", color = Color.Gray)
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = {
                    val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                        val options = ScanOptions()
                        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                        options.setPrompt("Scan Guide/Driver QR Code")
                        barcodeLauncher.launch(options)
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Orange),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier.fillMaxWidth().height(55.dp)
            ) {
                Text("Start Scanning", color = Color.White, fontWeight = FontWeight.Bold)
            }
        } else if (isChecking) {
            CircularProgressIndicator(color = Orange)
            Text("Verifying...", modifier = Modifier.padding(top = 16.dp))
        } else {
            // Result UI
            ResultCard(guideData, onRetry = { scanResult = null })
        }
    }
}

@Composable
fun ResultCard(data: Map<String, Any>?, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (data != null) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("VERIFIED GUIDE", color = Color(0xFF4CAF50), fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(16.dp))
                Text(data["name"]?.toString() ?: "Unknown", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("ID: ${data["id"] ?: "N/A"}", color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Rating: ⭐ ${data["rating"] ?: "5.0"}", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("NOT VERIFIED", color = Color.Red, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(16.dp))
                Text("This QR code is not in our verified registry. Please be careful.", textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Orange),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Scan Another")
            }
        }
    }
}
