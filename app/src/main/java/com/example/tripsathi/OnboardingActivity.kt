package com.example.tripsathi

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch

// ── Palette ───────────────────────────────────────────────────────────────────
private val OrangePrimary = Color(0xFFFF8C42)
private val OrangeDark    = Color(0xFFFF6B00)
private val OrangeDeep    = Color(0xFFE84F00)
private val OrangeLight   = Color(0xFFFFB347)
private val OrangeTint    = Color(0xFFFFF0E5)
private val CreamBg       = Color(0xFFFAFAFA)
private val CardWhite     = Color(0xFFFFFFFF)
private val TextDark      = Color(0xFF0D0D0D)
private val TextMuted     = Color(0xFF9A9A9A)
private val StrokeLine    = Color(0xFFEEEEEE)

class OnboardingActivity : BaseActivity() {
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
    val titleRes: Int,
    val descRes: Int,
    val icon: ImageVector,
    val heroImage: Int,
    val tagRes: Int
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PremiumOnboarding(onFinish: () -> Unit) {

    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val pages = listOf(
        OnboardPage(
            titleRes = R.string.onboard_title_1,
            descRes = R.string.onboard_desc_1,
            icon = Icons.Default.Shield,
            heroImage = R.drawable.hero_travel,
            tagRes = R.string.onboard_tag_1
        ),
        OnboardPage(
            titleRes = R.string.onboard_title_2,
            descRes = R.string.onboard_desc_2,
            icon = Icons.Default.LocationOn,
            heroImage = R.drawable.hero_map,
            tagRes = R.string.onboard_tag_2
        ),
        OnboardPage(
            titleRes = R.string.onboard_title_3,
            descRes = R.string.onboard_desc_3,
            icon = Icons.Default.Warning,
            heroImage = R.drawable.hero_sos,
            tagRes = R.string.onboard_tag_3
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // ── Top bar: back arrow + Skip ────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back arrow (hidden on first page)
                AnimatedVisibility(
                    visible = pagerState.currentPage > 0,
                    enter = fadeIn(tween(200)),
                    exit = fadeOut(tween(200))
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(StrokeLine)
                    ) {
                        Text(
                            text = "←",
                            fontSize = 18.sp,
                            color = TextDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (pagerState.currentPage == 0) {
                    Spacer(modifier = Modifier.size(40.dp))
                }

                TextButton(onClick = onFinish) {
                    Text(
                        text = stringResource(id = R.string.skip),
                        color = TextMuted,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ── Pager ─────────────────────────────────────────────────────────
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->

                val currentData = pages[page]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {

                    // ── Illustration area ─────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        // Soft orange blob behind illustration
                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            OrangeTint,
                                            CreamBg
                                        )
                                    )
                                )
                        )

                        // Tag pill floating above
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(OrangePrimary, OrangeLight)
                                        )
                                    )
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = currentData.icon,
                                        contentDescription = null,
                                        tint = CardWhite,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = stringResource(id = currentData.tagRes),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CardWhite,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Hero image
                            Image(
                                painter = painterResource(id = currentData.heroImage),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(200.dp)
                            )
                        }
                    }

                    // ── Text block at bottom ──────────────────────────────────
                    Column(
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        // Page number indicator
                        Text(
                            text = "0${page + 1} / 03",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangeDark,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = stringResource(id = currentData.titleRes),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark,
                            lineHeight = 36.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stringResource(id = currentData.descRes),
                            fontSize = 14.sp,
                            color = TextMuted,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // ── Dots + CTA row ────────────────────────────────────
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Dot indicators
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(pages.size) { index ->
                                    val isActive = pagerState.currentPage == index
                                    val width by animateDpAsState(
                                        targetValue = if (isActive) 24.dp else 7.dp,
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                        label = "dot_width"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .height(7.dp)
                                            .width(width)
                                            .clip(RoundedCornerShape(50))
                                            .background(
                                                if (isActive) OrangeDark else Color(0xFFDDDDDD)
                                            )
                                    )
                                }
                            }

                            // Arrow CTA button
                            val isLast = pagerState.currentPage == pages.size - 1

                            if (isLast) {
                                // "Get Started" wide pill button
                                Button(
                                    onClick = onFinish,
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = OrangeDark
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 10.dp
                                    ),
                                    contentPadding = PaddingValues(
                                        horizontal = 28.dp,
                                        vertical = 16.dp
                                    )
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.get_started),
                                        color = CardWhite,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            } else {
                                // Arrow circle button
                                Box(
                                    modifier = Modifier
                                        .size(58.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(OrangePrimary, OrangeDeep)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                pagerState.animateScrollToPage(
                                                    pagerState.currentPage + 1
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            text = "→",
                                            fontSize = 22.sp,
                                            color = CardWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // ── TripSathi logo top-center watermark ───────────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(OrangePrimary, OrangeDeep)
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = CardWhite,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "TripSathi",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
        }
    }
}