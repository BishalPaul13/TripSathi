package com.example.tripsathi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Palette (same across all screens) ────────────────────────────────────────
private val OrangePrimary  = Color(0xFFFF8C42)
private val OrangeDark     = Color(0xFFFF6B00)
private val OrangeDeep     = Color(0xFFE84F00)
private val OrangeLight    = Color(0xFFFFB347)
private val OrangeTint     = Color(0xFFFFF0E5)
private val OrangeBorder   = Color(0xFFFFD5B0)
private val CreamBg        = Color(0xFFFDF6EE)
private val FieldBg        = Color(0xFFFFF8F3)
private val CardWhite      = Color(0xFFFFFFFF)
private val TextDark       = Color(0xFF1A1A1A)
private val TextMuted      = Color(0xFF888888)
private val StrengthWeak   = Color(0xFFFF4444)
private val StrengthFair   = Color(0xFFFFB347)
private val StrengthGood   = Color(0xFF4CAF50)
private val StrengthEmpty  = Color(0xFFEEEEEE)

class RegisterActivity : BaseActivity() {

    private lateinit var authManager: AuthManager
    private val RC_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authManager = AuthManager(this)

        setContent {
            RegisterScreen(
                onGoogleSignUp = {
                    startActivityForResult(authManager.getSignInIntent(), RC_SIGN_IN)
                },
                onRegister = { name, email, password ->

                    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->

                            if (task.isSuccessful) {

                                val user = auth.currentUser

                                val profileUpdates =
                                    com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build()

                                user?.updateProfile(profileUpdates)

                                Toast.makeText(
                                    this,
                                    "Account Created ✅",
                                    Toast.LENGTH_SHORT
                                ).show()

                                startActivity(Intent(this, DashboardActivity::class.java))
                                finish()

                            } else {
                                Toast.makeText(
                                    this,
                                    task.exception?.message ?: "Registration Failed",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                },
                onLoginClick = {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            authManager.handleResult(
                data,
                onSuccess = {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                },
                onFailure = {
                    Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Password strength helper
// ─────────────────────────────────────────────────────────────────────────────
private fun passwordStrength(password: String): Int {
    // Returns 0..4
    if (password.isEmpty()) return 0
    var score = 0
    if (password.length >= 8)                          score++
    if (password.any { it.isUpperCase() })             score++
    if (password.any { it.isDigit() })                 score++
    if (password.any { "!@#\$%^&*()_+-=[]{}|;':\",./<>?".contains(it) }) score++
    return score
}

private fun strengthLabel(score: Int) = when (score) {
    0    -> ""
    1    -> "Weak"
    2    -> "Fair"
    3    -> "Good"
    4    -> "Strong ✓"
    else -> ""
}

private fun strengthColor(score: Int) = when (score) {
    1    -> StrengthWeak
    2    -> StrengthFair
    3, 4 -> StrengthGood
    else -> StrengthEmpty
}

// ─────────────────────────────────────────────────────────────────────────────
//  Main composable
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun RegisterScreen(
    onGoogleSignUp: () -> Unit,
    onRegister: (name: String, email: String, password: String) -> Unit,
    onLoginClick: () -> Unit
) {
    var name            by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword    by remember { mutableStateOf(false) }
    var showConfirm     by remember { mutableStateOf(false) }

    val strength = passwordStrength(password)
    val passwordsMatch = password == confirmPassword && confirmPassword.isNotEmpty()

    val heroGradient = Brush.linearGradient(
        colorStops = arrayOf(0.0f to OrangePrimary, 1.0f to OrangeDeep)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Hero section ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(heroGradient)
                    .padding(top = 52.dp, bottom = 36.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Create Account",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CardWhite,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Join TripSathi — Travel Smart, Stay Safe",
                        fontSize = 13.sp,
                        color = CardWhite.copy(alpha = 0.82f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ── Wave divider ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .background(
                        color = CreamBg,
                        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
                    )
            )

            // ── Form content ──────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Section label
                Text(
                    text = "YOUR DETAILS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeDark,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                // ── Form card ─────────────────────────────────────────────────
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {

                        // Orange top accent bar
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

                            // Name field
                            RegisterField(
                                value = name,
                                onValueChange = { name = it },
                                label = "FULL NAME",
                                placeholder = "e.g. Rahul Sharma",
                                icon = Icons.Default.Person,
                                keyboardType = KeyboardType.Text
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Email field
                            RegisterField(
                                value = email,
                                onValueChange = { email = it },
                                label = "EMAIL ADDRESS",
                                placeholder = "e.g. rahul@gmail.com",
                                icon = Icons.Default.Email,
                                keyboardType = KeyboardType.Email
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Password field
                            RegisterField(
                                value = password,
                                onValueChange = { password = it },
                                label = "PASSWORD",
                                placeholder = "Min 8 chars, upper, number, symbol",
                                icon = Icons.Default.Lock,
                                keyboardType = KeyboardType.Password,
                                isPassword = true,
                                showPassword = showPassword,
                                onTogglePassword = { showPassword = !showPassword }
                            )

                            // Strength bar
                            if (password.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                PasswordStrengthBar(strength = strength)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = strengthLabel(strength),
                                    fontSize = 10.sp,
                                    color = strengthColor(strength),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Confirm password
                            RegisterField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = "CONFIRM PASSWORD",
                                placeholder = "Re-enter your password",
                                icon = Icons.Default.Lock,
                                keyboardType = KeyboardType.Password,
                                isPassword = true,
                                showPassword = showConfirm,
                                onTogglePassword = { showConfirm = !showConfirm },
                                isError = confirmPassword.isNotEmpty() && !passwordsMatch,
                                isSuccess = passwordsMatch
                            )

                            if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Passwords do not match",
                                    fontSize = 10.sp,
                                    color = StrengthWeak
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Google sign-up button ─────────────────────────────────────
                GoogleSignUpButton(onClick = onGoogleSignUp)

                Spacer(modifier = Modifier.height(12.dp))

                // ── Divider ───────────────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = 0.5.dp,
                        color = Color(0xFFE8DDD4)
                    )
                    Text(
                        text = "  or register with email  ",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = 0.5.dp,
                        color = Color(0xFFE8DDD4)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Register button ───────────────────────────────────────────
                Button(
                    onClick = {
                        if (name.isNotBlank() && email.isNotBlank() &&
                            password.isNotBlank() && passwordsMatch && strength >= 3
                        ) {
                            onRegister(name, email, password)
                        }
                    },
                    enabled = name.isNotBlank() && email.isNotBlank() &&
                            passwordsMatch && strength >= 3,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangeDark,
                        disabledContainerColor = OrangeBorder
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(
                        text = "Create My Account  →",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CardWhite
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ── Login link ────────────────────────────────────────────────
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Already have an account? ",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "Sign In",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangeDark,
                        modifier = Modifier.clickable { onLoginClick() }
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Reusable text field
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RegisterField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    isError: Boolean = false,
    isSuccess: Boolean = false
) {
    val borderColor = when {
        isError   -> StrengthWeak
        isSuccess -> StrengthGood
        value.isNotEmpty() -> OrangeDark
        else      -> OrangeBorder
    }

    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 0.6.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(placeholder, fontSize = 13.sp, color = Color(0xFFCCCCCC))
            },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (value.isNotEmpty()) OrangeDark else Color(0xFFCCCCCC),
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = if (isPassword && onTogglePassword != null) {
                {
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = Color(0xFFCCCCCC),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !showPassword)
                PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangeDark,
                unfocusedBorderColor = borderColor,
                errorBorderColor = StrengthWeak,
                focusedContainerColor = FieldBg,
                unfocusedContainerColor = FieldBg,
                cursorColor = OrangeDark
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Password strength bar (4 segments)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PasswordStrengthBar(strength: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(4) { index ->
            val filled = index < strength
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (filled) strengthColor(strength) else StrengthEmpty
                    )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Google sign-up button
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun GoogleSignUpButton(onClick: () -> Unit) {

    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple() // ✅ NEW API (no warning)
            ) {
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "Google Sign Up",
                modifier = Modifier.size(22.dp),
                tint = Color.Unspecified
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Continue with Google",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )
        }
    }
}