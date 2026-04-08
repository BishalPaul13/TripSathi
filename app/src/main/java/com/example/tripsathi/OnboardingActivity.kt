package com.example.tripsathi

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Shield
import kotlinx.coroutines.launch

// ── Palette ──────────────────────────────────────────────────────────────────
private val OrangePrimary  = Color(0xFFFF8C42)
private val OrangeDark     = Color(0xFFFF6B00)
private val OrangeDeep     = Color(0xFFE84F00)
private val OrangeLight    = Color(0xFFFFB347)
private val OrangeTint     = Color(0xFFFFF0E5)
private val CreamBg        = Color(0xFFFDF6EE)   // warm cream background
private val CardWhite      = Color(0xFFFFFFFF)
private val TextDark       = Color(0xFF1A1A1A)
private val TextMuted      = Color(0xFF888888)

class OnboardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PremiumOnboarding {
                startActivity(Intent(this, LandingActivity::class.java))
                finish()
            }
        }
    }
}

data class OnboardPage(
    val title: String,
    val desc: String,
    val icon: ImageVector,
    val heroImage: Int,          // drawable res id — put your illustration here
    val cardLabel: String,
    val cardTitle: String,
    val cardDesc: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PremiumOnboarding(onFinish: () -> Unit) {

    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val pages = listOf(
        OnboardPage(
            title = "Welcome to TripSathi",
            desc = "Your smart AI-powered travel safety companion for every journey.",
            icon = Icons.Default.Shield,
            heroImage = R.drawable.hero_travel,   // 🖼 add hero_travel.png to drawable
            cardLabel = "AI-POWERED",
            cardTitle = "Real Protection, Everywhere",
            cardDesc = "Risk detection, emergency alerts, and live tracking built for every traveler."
        ),
        OnboardPage(
            title = "Live GPS Tracking",
            desc = "Always know where you are — even in unfamiliar places.",
            icon = Icons.Default.LocationOn,
            heroImage = R.drawable.hero_map,       // 🖼 add hero_map.png to drawable
            cardLabel = "FEATURE",
            cardTitle = "Interactive Live Map",
            cardDesc = "Your position updates in real-time on Google Maps with geo-fence detection."
        ),
        OnboardPage(
            title = "SOS Protection",
            desc = "One tap is all it takes to get help instantly.",
            icon = Icons.Default.Warning,
            heroImage = R.drawable.hero_sos,       // 🖼 add hero_sos.png to drawable
            cardLabel = "EMERGENCY",
            cardTitle = "One-Tap SOS System",
            cardDesc = "Triggers alarm, vibration, sends your live location via SMS, and dials emergency contacts."
        )
    )

    val heroGradient = Brush.linearGradient(
        colorStops = arrayOf(
            0.0f to OrangePrimary,
            1.0f to OrangeDeep
        )
    )

    // Cream background for entire screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->

                AnimatedContent(
                    targetState = page,
                    transitionSpec = {
                        fadeIn(tween(500)) togetherWith fadeOut(tween(300))
                    },
                    label = "PageContent"
                ) { targetPage ->

                    val currentData = pages[targetPage]

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // ── Hero section (orange gradient top half) ─────────
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(heroGradient)
                                .padding(top = 20.dp, bottom = 32.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Logo row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(CardWhite)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = OrangeDark,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "TripSathi",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CardWhite
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Hero illustration
                                Image(
                                    painter = painterResource(id = currentData.heroImage),
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(160.dp)
                                        .clip(CircleShape)
                                )
                            }
                        }

                        // ── Wave divider ─────────────────────────────────────
                        WaveDivider()

                        // ── Content on cream background ──────────────────────
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = currentData.title,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDark,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = currentData.desc,
                                fontSize = 13.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // ── Premium info card ────────────────────────────
                            OnboardInfoCard(
                                label = currentData.cardLabel,
                                title = currentData.cardTitle,
                                description = currentData.cardDesc
                            )
                        }
                    }
                }
            }

            // ── Dots ─────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val isActive = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(6.dp)
                            .width(if (isActive) 22.dp else 6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (isActive) OrangeDark else Color(0xFFDDDDDD))
                    )
                }
            }

            // ── Navigation buttons ────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onFinish) {
                    Text("Skip", color = TextMuted, fontSize = 14.sp)
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onFinish()
                        }
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeDark),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next  →",
                        color = CardWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// ── Curved wave divider between hero and cream content ───────────────────────
@Composable
private fun WaveDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(
                color = CreamBg,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
    )
}

// ── White info card with orange top accent ────────────────────────────────────
@Composable
private fun OnboardInfoCard(label: String, title: String, description: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(OrangePrimary, OrangeLight)
                        )
                    )
            )

            Column(modifier = Modifier.padding(18.dp)) {

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(OrangeTint)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangeDark,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = TextMuted,
                    lineHeight = 20.sp
                )
            }
        }
    }
}