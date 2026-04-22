package com.example.tripsathi

import com.google.firebase.database.FirebaseDatabase

object FirebaseProvider {
    val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }
}
