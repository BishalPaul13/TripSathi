package com.example.tripsathi

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

private val Orange = Color(0xFFFF6B00)
private val Bg = Color(0xFFF7F7F7)

class MedicalProfileActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MedicalProfileScreen() }
    }
}

@Composable
fun MedicalProfileScreen() {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseDatabase.getInstance().getReference("health")

    var blood by remember { mutableStateOf("") }
    var allergy by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("") }
    var meds by remember { mutableStateOf("") }
    var donor by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            db.child(uid).get().addOnSuccessListener { snapshot ->
                val data = snapshot.value as? Map<String, Any>
                if (data != null) {
                    blood = data["blood"]?.toString() ?: ""
                    allergy = data["allergy"]?.toString() ?: ""
                    condition = data["condition"]?.toString() ?: ""
                    meds = data["meds"]?.toString() ?: ""
                    donor = data["donor"]?.toString() ?: ""
                }
                isLoading = false
            }.addOnFailureListener {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 🔙 HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { activity.finish() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            Text(
                text = stringResource(id = R.string.medical_profile),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Orange)
            }
        } else {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    MedicalField(
                        label = stringResource(id = R.string.blood_group),
                        value = blood,
                        onValueChange = { blood = it },
                        icon = Icons.Default.Bloodtype
                    )
                    MedicalField(
                        label = stringResource(id = R.string.allergies),
                        value = allergy,
                        onValueChange = { allergy = it },
                        icon = Icons.Default.Warning
                    )
                    MedicalField(
                        label = stringResource(id = R.string.chronic_conditions),
                        value = condition,
                        onValueChange = { condition = it },
                        icon = Icons.Default.History
                    )
                    MedicalField(
                        label = stringResource(id = R.string.medications),
                        value = meds,
                        onValueChange = { meds = it },
                        icon = Icons.Default.Medication
                    )
                    MedicalField(
                        label = stringResource(id = R.string.organ_donor),
                        value = donor,
                        onValueChange = { donor = it },
                        icon = Icons.Default.Favorite
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val data = mapOf(
                                "blood" to blood,
                                "allergy" to allergy,
                                "condition" to condition,
                                "meds" to meds,
                                "donor" to donor
                            )
                            user?.uid?.let {
                                db.child(it).setValue(data).addOnSuccessListener {
                                    Toast.makeText(context, "Medical Profile Updated ✅", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Orange)
                    ) {
                        Text(stringResource(id = R.string.save), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MedicalField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(icon, null, tint = Orange, modifier = Modifier.size(20.dp)) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Orange,
                unfocusedBorderColor = Color.LightGray
            ),
            singleLine = true
        )
    }
}
