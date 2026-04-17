package com.example.tripsathi

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.res.stringResource

private val OrangePrimary  = Color(0xFFFF8C42)
private val OrangeDark     = Color(0xFFFF6B00)
private val OrangeDeep     = Color(0xFFE84F00)
private val CardWhite      = Color(0xFFFFFFFF)
private val TextMuted      = Color(0xFF888888)
private val CreamBg        = Color(0xFFFDF6EE)
private val black        = Color(0xFF000000)

class LandingActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PremiumLandingScreen(
                onStartClick = {
                    startActivity(Intent(this, RegisterActivity::class.java))
                },
                onLoginClick = {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            )
        }
    }
}

data class FeatureItem(
    val icon: ImageVector,
    val titleRes: Int,
    val descRes: Int
)

@Composable
fun PremiumLandingScreen(
    onStartClick: () -> Unit,
    onLoginClick: () -> Unit
) {

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    val heroGradient = Brush.linearGradient(
        colorStops = arrayOf(
            0.0f to CreamBg,
            1.0f to CreamBg
        )
    )

    val features = listOf(
        FeatureItem(Icons.Default.LocationOn, R.string.feature_1_title, R.string.feature_1_desc),
        FeatureItem(Icons.Default.Warning, R.string.feature_2_title, R.string.feature_2_desc),
        FeatureItem(Icons.Default.Shield, R.string.feature_3_title, R.string.feature_3_desc)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBg)
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(900)) + slideInVertically(tween(900)) { it / 3 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // 🔥 HERO
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(heroGradient)
                        .padding(top = 48.dp, bottom = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        Text(
                            text = stringResource(id = R.string.landing_title),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = black
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = stringResource(id = R.string.landing_tagline),
                            fontSize = 14.sp,
                            color = black.copy(alpha = 0.82f)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Image(
                            painter = painterResource(id = R.drawable.tripsathilogo),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(180.dp)
                                .clip(CircleShape)
                        )
                    }
                }

                // BODY
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        stringResource(id = R.string.welcome_msg),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            features.forEach {
                                LandingFeatureRow(it)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    // ✅ CONTINUE BUTTON
                    Button(
                        onClick = onStartClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(stringResource(id = R.string.continue_btn))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ✅ LOGIN BUTTON
                    TextButton(onClick = onLoginClick) {
                        Text(stringResource(id = R.string.already_have_account_login))
                    }
                }
            }
        }
    }
}

@Composable
private fun LandingFeatureRow(feature: FeatureItem) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        Icon(
            imageVector = feature.icon,
            contentDescription = null,
            tint = OrangeDark
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(stringResource(id = feature.titleRes), fontWeight = FontWeight.Bold)
            Text(stringResource(id = feature.descRes), fontSize = 12.sp, color = TextMuted)
        }
    }
}