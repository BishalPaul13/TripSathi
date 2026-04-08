package com.example.tripsathi

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
import kotlinx.coroutines.launch

private val Orange = Color(0xFFFF6B00)
private val OrangeLight = Color(0xFFFFEDE5)
private val Bg = Color(0xFFF7F7F7)

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DashboardUI()
        }
    }
}

@Composable
fun DashboardUI() {

    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // 🔥 GET REAL USER
    val user = FirebaseAuth.getInstance().currentUser
    val userName = user?.displayName ?: user?.email?.substringBefore("@") ?: "User"

    var searchQuery by remember { mutableStateOf("") }

    // 🔥 LOGOUT DIALOG STATE
    var showLogoutDialog by remember { mutableStateOf(false) }

    val allItems = listOf(
        Triple("Identity", Icons.Default.Person, "identity"),
        Triple("Safety", Icons.Default.Shield, "safety"),
        Triple("Health", Icons.Default.Favorite, "health"),
        Triple("Alerts", Icons.Default.Warning, "alerts"),
        Triple("Travel", Icons.Default.Map, "travel"),
        Triple("Transport", Icons.Default.DirectionsCar, "transport")
    )

    val filteredItems = allItems.filter {
        it.first.contains(searchQuery, ignoreCase = true)
    }

    // 🔥 LOGOUT CONFIRMATION DIALOG
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false

                        FirebaseAuth.getInstance().signOut()

                        val intent = Intent(context, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Logout", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Cancel")
                }
            },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "TripSathi",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Orange
                )

                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            showLogoutDialog = true
                        }
                    },
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {

        Scaffold(
            bottomBar = { BottomBar() }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Bg)
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {

                // 🔥 HEADER (NOW REAL NAME)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        "Hello, $userName",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Notifications, contentDescription = null)

                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Menu",
                            modifier = Modifier.clickable {
                                scope.launch { drawerState.open() }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 🔍 SEARCH BAR (UNCHANGED)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text("Search", color = Color.Gray)
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    Icon(Icons.Default.Mic, contentDescription = null, tint = Orange)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 📄 MAIN CARD
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Safety Identity", fontWeight = FontWeight.Bold)
                            Text("Active", color = Orange, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("Your digital safety ID is active")

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "View details",
                            color = Orange,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                context.startActivity(Intent(context, QRActivity::class.java))
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { 0.7f },
                            color = Orange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🔥 TWO CARDS
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SmallCard("Recent activity", Icons.Default.History)
                    SmallCard("My documents", Icons.Default.Folder)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🔘 CATEGORY SWITCH
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(30.dp))
                        .padding(4.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(OrangeLight, RoundedCornerShape(30.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Services", color = Orange)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Categories")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🔥 GRID
                Column {
                    filteredItems.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach {
                                GridItem(
                                    title = it.first,
                                    icon = it.second,
                                    onClick = {
                                        if (it.third == "identity") {
                                            context.startActivity(
                                                Intent(context, UserInfoActivity::class.java)
                                            )
                                        }
                                        if (it.third == "safety") {
                                            context.startActivity(
                                                Intent(context, SafetyActivity::class.java)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun RowScope.SmallCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = Orange)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RowScope.GridItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit = {}
) {

    Column(
        modifier = Modifier
            .weight(1f)
            .background(Color.White, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(icon, contentDescription = null, tint = Orange)

        Spacer(modifier = Modifier.height(8.dp))

        Text(title, fontSize = 12.sp)
    }
}

@Composable
fun BottomBar() {

    val context = LocalContext.current

    NavigationBar(containerColor = Color.White) {

        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = false,
            onClick = {
                context.startActivity(Intent(context, MainActivity::class.java))
            },
            icon = { Icon(Icons.Default.Map, contentDescription = null) },
            label = { Text("Map") }
        )

        NavigationBarItem(
            selected = false,
            onClick = {
                context.startActivity(Intent(context, QRScannerActivity::class.java))
            },
            icon = { Icon(Icons.Default.QrCode, contentDescription = null) },
            label = { Text("QR") }
        )

        NavigationBarItem(
            selected = false,
            onClick = {
                context.startActivity(Intent(context, ProfileActivity::class.java))
            },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("Profile") }
        )
    }
}
