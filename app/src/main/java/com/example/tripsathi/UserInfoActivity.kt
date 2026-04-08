package com.example.tripsathi

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

private val Orange = Color(0xFFFF6B00)
private val Bg = Color(0xFFF7F7F7)

class UserInfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { UserInfoScreen() }
    }
}

@Composable
fun UserInfoScreen() {

    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseDatabase.getInstance().getReference("users")

    var blood by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var donor by remember { mutableStateOf("") }

    var isChecked by remember { mutableStateOf(false) }
    var isEditable by remember { mutableStateOf(true) }

    // ✅ PREFILL DATA
    LaunchedEffect(Unit) {
        user?.uid?.let {
            db.child(it).get().addOnSuccessListener { snap ->
                val data = snap.value as? Map<String, Any>
                data?.let {
                    blood = it["blood"]?.toString() ?: ""
                    contact = it["contact"]?.toString() ?: ""
                    address = it["address"]?.toString() ?: ""
                    donor = it["donor"]?.toString() ?: ""

                    isEditable = false // 🔒 LOCK
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text("Review your information", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔥 PERSONAL
        InfoCard("Personal", onEditClick = { isEditable = true }) {
            Text("Name: ${user?.displayName ?: "N/A"}")
            Text("Email: ${user?.email ?: "N/A"}")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🔥 CONTACT
        InfoCard("Contact", onEditClick = { isEditable = true }) {
            OutlinedTextField(
                value = contact,
                onValueChange = { contact = it },
                label = { Text("Emergency Contact") },
                enabled = isEditable,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🔥 SAFETY
        InfoCard("Safety Details", onEditClick = { isEditable = true }) {

            OutlinedTextField(
                value = blood,
                onValueChange = { blood = it },
                label = { Text("Blood Group") },
                enabled = isEditable,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Home Address") },
                enabled = isEditable,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = donor,
                onValueChange = { donor = it },
                label = { Text("Organ Donor (Yes/No)") },
                enabled = isEditable,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { isChecked = it },
                colors = CheckboxDefaults.colors(checkedColor = Orange)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("I confirm that the above information is correct", fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {

                val data = mapOf(
                    "name" to user?.displayName,
                    "email" to user?.email,
                    "blood" to blood,
                    "contact" to contact,
                    "address" to address,
                    "donor" to donor
                )

                user?.uid?.let {
                    db.child(it).setValue(data)
                }

                context.startActivity(Intent(context, VerifiedActivity::class.java))

            },
            enabled = isChecked,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Orange)
        ) {
            Text("Continue", color = Color.White)
        }
    }
}

// ✅ FIXED CARD
@Composable
fun InfoCard(
    title: String,
    onEditClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, fontWeight = FontWeight.Bold)
                if (title != "Personal") {
                    Text(
                        "Edit",
                        color = Orange,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { onEditClick() } // ✅ FIX
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            content()
        }
    }
}