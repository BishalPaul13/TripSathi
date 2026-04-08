package com.example.tripsathi

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val Orange = Color(0xFFFF6B00)
private val Bg = Color(0xFFF7F7F7)

class VerifiedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VerifiedScreen {
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            }
        }
    }
}

@Composable
fun VerifiedScreen(onFinish: () -> Unit) {

    LaunchedEffect(Unit) {
        delay(2000)
        onFinish()
    }

    val infiniteTransition = rememberInfiniteTransition()

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 🔥 FIXED SIZE BOX (NO LAYOUT SHIFT)
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {

            repeat(4) { i ->
                Box(
                    modifier = Modifier
                        .size((120 + i * 30).dp * scale)
                        .background(
                            Orange.copy(alpha = 0.1f),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
            }

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Orange, shape = androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", color = Color.White, fontSize = 40.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Verified",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Orange
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Your information has been verified\nand your safety ID is ready.",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            "Redirecting...",
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}