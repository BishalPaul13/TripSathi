package com.example.tripsathi

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

private val Orange = Color(0xFFFF6B00)
private val Bg = Color(0xFFF6ECE5)

class QRActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { QRScreen() }
    }
}

@Composable
fun QRScreen() {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseDatabase.getInstance().getReference("users")

    var data by remember { mutableStateOf<Map<String, Any>?>(null) }

    LaunchedEffect(Unit) {
        user?.uid?.let {
            db.child(it).get().addOnSuccessListener {
                data = it.value as? Map<String, Any>
            }
        }
    }

    val qrText = """
        Name: ${user?.displayName}
        Email: ${user?.email}
        Blood: ${data?.get("blood")}
        Contact: ${data?.get("contact")}
        Address: ${data?.get("address")}
        Donor: ${data?.get("donor")}
    """.trimIndent()

    val qr = remember(qrText) { generateQR(qrText) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            "Your Safety ID",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 🔥 GLASS CARD
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.85f)
            ),
            elevation = CardDefaults.cardElevation(10.dp)
        ) {

            Column(
                modifier = Modifier
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text("TripSathi Digital ID", fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))

                // QR
                qr?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(200.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // USER INFO
                Text(user?.displayName ?: "", fontWeight = FontWeight.Bold)
                Text(user?.email ?: "", fontSize = 12.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(12.dp))

                data?.let {
                    InfoRow("Blood", it["blood"])
                    InfoRow("Contact", it["contact"])
                    InfoRow("Address", it["address"])
                    InfoRow("Donor", it["donor"])
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🔥 ACTION BUTTONS
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // SHARE
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    intent.putExtra(Intent.EXTRA_TEXT, qrText)
                    context.startActivity(Intent.createChooser(intent, "Share via"))
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Orange)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share", color = Orange)
            }

            // SCAN
            Button(
                onClick = {
                    // later connect scanner
                },
                colors = ButtonDefaults.buttonColors(containerColor = Orange),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Scan", color = Color.White)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: Any?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value?.toString() ?: "-", fontSize = 12.sp)
    }
}

// QR GENERATOR
fun generateQR(text: String): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
        val bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)

        for (x in 0 until 512) {
            for (y in 0 until 512) {
                bmp.setPixel(
                    x, y,
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK
                    else android.graphics.Color.WHITE
                )
            }
        }
        bmp
    } catch (e: Exception) {
        null
    }
}