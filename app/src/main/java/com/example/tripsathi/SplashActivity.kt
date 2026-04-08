package com.example.tripsathi

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SplashScreen {
                checkUserAndNavigate()
            }
        }
    }

    private fun checkUserAndNavigate() {
        val auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            // ✅ User already logged in → go to Dashboard
            startActivity(Intent(this, DashboardActivity::class.java))
        } else {
            // ❌ Not logged in → go to Onboarding
            startActivity(Intent(this, OnboardingActivity::class.java))
        }

        finish()
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {

    LaunchedEffect(true) {
        delay(2000) // splash delay
        onTimeout()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "TripSathi",
            fontSize = 32.sp
        )
    }
}