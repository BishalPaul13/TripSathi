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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

// ── Palette ──────────────────────────────────────────────────────────────────
private val OrangePrimary  = Color(0xFFFF8C42)
private val OrangeDark     = Color(0xFFFF6B00)
private val OrangeDeep     = Color(0xFFE84F00)
private val CreamBg        = Color(0xFFFDF6EE)
private val CardWhite      = Color(0xFFFFFFFF)
private val FieldBg        = Color(0xFFFFF8F3)
private val TextDark       = Color(0xFF1A1A1A)
private val TextMuted      = Color(0xFF888888)

class LoginActivity : BaseActivity() {

    private lateinit var authManager: AuthManager
    private val RC_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authManager = AuthManager(this)

        setContent {
            LoginScreen(
                onGoogleSignIn = {
                    startActivityForResult(authManager.getSignInIntent(), RC_SIGN_IN)
                },
                onLogin = { email, password ->
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                startActivity(Intent(this, DashboardActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this, task.exception?.message ?: "Login Failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                },
                onSignUpClick = {
                    startActivity(Intent(this, RegisterActivity::class.java))
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

@Composable
fun LoginScreen(
    onGoogleSignIn: () -> Unit,
    onLogin: (String, String) -> Unit,
    onSignUpClick: () -> Unit
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

            // Hero Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(heroGradient)
                    .padding(top = 64.dp, bottom = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(id = R.string.welcome_back),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CardWhite
                    )
                    Text(
                        text = stringResource(id = R.string.sign_in_desc),
                        fontSize = 14.sp,
                        color = CardWhite.copy(alpha = 0.8f)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .offset(y = (-20).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Login Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {

                        LoginField(
                            value = email,
                            onValueChange = { email = it },
                            label = stringResource(id = R.string.email_label),
                            placeholder = stringResource(id = R.string.email_placeholder),
                            icon = Icons.Default.Email,
                            keyboardType = KeyboardType.Email
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LoginField(
                            value = password,
                            onValueChange = { password = it },
                            label = stringResource(id = R.string.password_label),
                            placeholder = stringResource(id = R.string.password_placeholder),
                            icon = Icons.Default.Lock,
                            keyboardType = KeyboardType.Password,
                            isPassword = true,
                            showPassword = showPassword,
                            onTogglePassword = { showPassword = !showPassword }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { onLogin(email, password) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeDark)
                        ) {
                            Text(
                                stringResource(id = R.string.sign_in_btn),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 🔥 PREMIUM GOOGLE BUTTON
                val interactionSource = remember { MutableInteractionSource() }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple() // ✅ NEW API (NO WARNING)
                        ) {
                            onGoogleSignIn()
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
                            contentDescription = "Google Sign In",
                            modifier = Modifier.size(22.dp),
                            tint = Color.Unspecified
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = stringResource(id = R.string.continue_with_google),
                            color = TextDark,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row {
                    Text(stringResource(id = R.string.dont_have_account), color = TextMuted)
                    Text(
                        stringResource(id = R.string.sign_up),
                        color = OrangeDark,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onSignUpClick() }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onTogglePassword: (() -> Unit)? = null
) {
    Column {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 14.sp) },
            leadingIcon = { Icon(icon, null, modifier = Modifier.size(18.dp)) },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = onTogglePassword!!) {
                        Icon(
                            if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            null
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !showPassword)
                PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = FieldBg,
                unfocusedContainerColor = FieldBg
            )
        )
    }
}