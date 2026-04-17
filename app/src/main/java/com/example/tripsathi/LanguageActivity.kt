package com.example.tripsathi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
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
private val Bg = Color(0xFFF6ECE5)

class LanguageActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LanguageSelectionScreen()
        }
    }
}

@Composable
fun LanguageSelectionScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Language,
            contentDescription = null,
            tint = Orange,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Choose Your Language",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Text(
            text = "अपनी भाषा चुनें",
            fontSize = 20.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(40.dp))

        LanguageButton(
            text = "English",
            subText = "अंग्रेजी, ఇంగ్లీష్",
            onClick = {
                saveLanguageAndProceed(context, "en")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LanguageButton(
            text = "Hindi",
            subText = "हिंदी",
            onClick = {
                saveLanguageAndProceed(context, "hi")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LanguageButton(
            text = "Telugu",
            subText = "తెలుగు",
            onClick = {
                saveLanguageAndProceed(context, "te")
            }
        )
    }
}

@Composable
fun LanguageButton(text: String, subText: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = subText, fontSize = 14.sp, color = Color.Gray)
            }
            RadioButton(selected = false, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = Orange))
        }
    }
}

fun saveLanguageAndProceed(context: Context, langCode: String) {
    val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
    prefs.edit().putString("My_Lang", langCode).apply()
    prefs.edit().putBoolean("isFirstBoot", false).apply()

    // Redirect to Onboarding after language selection
    val intent = Intent(context, OnboardingActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    context.startActivity(intent)
}
