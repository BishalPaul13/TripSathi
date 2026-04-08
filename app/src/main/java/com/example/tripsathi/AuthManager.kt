package com.example.tripsathi

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthManager(private val activity: Activity) {

    private val auth = FirebaseAuth.getInstance()

    private val googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("419550298572-gkasrhq11ii3f7rovo9cvce74rbla2vd.apps.googleusercontent.com") // 🔥 replace
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    fun handleResult(
        data: Intent?,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.result
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            auth.signInWithCredential(credential)
                .addOnCompleteListener(activity) {
                    if (it.isSuccessful) onSuccess()
                    else onFailure("Auth Failed")
                }

        } catch (e: Exception) {
            onFailure(e.message ?: "Error")
        }
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun logout() {
        auth.signOut()
    }
}