package com.example.tripsathi

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.google.firebase.database.*

private val Orange = Color(0xFFFF6B00)
private val Bg = Color(0xFFF7F7F7)

data class Contact(
    val id: String = "",
    val name: String = "",
    val phone: String = ""
)

class ContactsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ContactsScreen()
        }
    }
}

@Composable
fun ContactsScreen() {

    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseProvider.database.reference

    var contacts by remember { mutableStateOf(listOf<Contact>()) }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // 🔥 FETCH CONTACTS
    LaunchedEffect(Unit) {
        user?.uid?.let { uid ->
            db.child("users").child(uid).child("contacts")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val list = mutableListOf<Contact>()
                        for (child in snapshot.children) {
                            val c = child.getValue(Contact::class.java)
                            if (c != null) {
                                list.add(c.copy(id = child.key ?: ""))
                            }
                        }
                        contacts = list
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
                    }
                })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(16.dp)
    ) {

        // 🔙 HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = null,
                modifier = Modifier.clickable {
                    (context as ComponentActivity).finish()
                })

            Text("Emergency Contacts", fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.width(24.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ➕ ADD CONTACT
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {

                Text("Add Contact", fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (name.isNotEmpty() && phone.isNotEmpty()) {

                            val contactId = db.push().key ?: return@Button

                            val contact = Contact(contactId, name, phone)

                            user?.uid?.let { uid ->
                                db.child("users")
                                    .child(uid)
                                    .child("contacts")
                                    .child(contactId)
                                    .setValue(contact)
                                    .addOnSuccessListener {
                                        name = ""
                                        phone = ""
                                        Toast.makeText(context, "Contact saved", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { error ->
                                        Toast.makeText(context, error.localizedMessage ?: "Failed to save contact", Toast.LENGTH_LONG).show()
                                    }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange)
                ) {
                    Text("Save Contact", color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 📋 CONTACT LIST
        Text("Saved Contacts", fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn {

            items(contacts) { contact ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column {
                            Text(contact.name, fontWeight = FontWeight.Bold)
                            Text(contact.phone, color = Color.Gray, fontSize = 12.sp)
                        }

                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.clickable {

                                user?.uid?.let { uid ->
                                    db.child("users")
                                        .child(uid)
                                        .child("contacts")
                                        .child(contact.id)
                                        .removeValue()
                                        .addOnFailureListener { error ->
                                            Toast.makeText(context, error.localizedMessage ?: "Failed to delete contact", Toast.LENGTH_LONG).show()
                                        }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
