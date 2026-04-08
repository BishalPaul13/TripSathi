package com.example.tripsathi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

// 🎨 SAME COLORS
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

class LoginActivity : ComponentActivity() {

    private lateinit var authManager: AuthManager
    private val RC_SIGN_IN = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        authManager = AuthManager(this)

        setContent {
            LoginScreen(
                onGoogleLogin = {
                    startActivityForResult(authManager.getSignInIntent(), RC_SIGN_IN)
                },
                onLogin = { email, password ->

                    // 🔥 FIREBASE LOGIN
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->

                            if (task.isSuccessful) {

                                Toast.makeText(
                                    this,
                                    "Login Successful ✅",
                                    Toast.LENGTH_SHORT
                                ).show()

                                startActivity(Intent(this, DashboardActivity::class.java))
                                finish()

                            } else {
                                Toast.makeText(
                                    this,
                                    task.exception?.message ?: "Login Failed",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                },
                onRegisterClick = {
                    startActivity(Intent(this, RegisterActivity::class.java))
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

// 🎯 UI
@Composable
fun LoginScreen(
    onGoogleLogin: () -> Unit,
    onLogin: (String, String) -> Unit,
    onRegisterClick: () -> Unit
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

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

            // 🔥 HERO
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(heroGradient)
                    .padding(top = 52.dp, bottom = 36.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Welcome Back",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CardWhite
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Login to continue your journey",
                        fontSize = 13.sp,
                        color = CardWhite.copy(alpha = 0.82f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // EMAIL
                LoginField(
                    value = email,
                    onValueChange = { email = it },
                    label = "EMAIL",
                    icon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(14.dp))

                // PASSWORD
                LoginField(
                    value = password,
                    onValueChange = { password = it },
                    label = "PASSWORD",
                    icon = Icons.Default.Lock,
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    showPassword = showPassword,
                    onTogglePassword = { showPassword = !showPassword }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // LOGIN BUTTON
                Button(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            onLogin(email, password)
                        }
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text("Login →", color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // GOOGLE
                GoogleLoginButton(onClick = onGoogleLogin)

                Spacer(modifier = Modifier.height(20.dp))

                // REGISTER LINK
                Row {
                    Text("Don't have an account? ", color = TextMuted)
                    Text(
                        "Sign Up",
                        color = OrangeDark,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onRegisterClick() }
                    )
                }
            }
        }
    }
}

// 🔹 FIELD
@Composable
fun LoginField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onTogglePassword: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null) },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { onTogglePassword?.invoke() }) {
                    Icon(
                        if (showPassword) Icons.Default.VisibilityOff
                        else Icons.Default.Visibility,
                        null
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !showPassword)
            PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth()
    )
}

// 🔹 GOOGLE BUTTON
@Composable
fun GoogleLoginButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Continue with Google")
    }
}