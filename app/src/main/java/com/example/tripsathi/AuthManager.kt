package com.example.tripsathi

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthManager(private val activity: Activity) {

    private val auth = FirebaseAuth.getInstance()

    private val googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }

    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    fun handleResult(
        data: Intent?,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                .getResult(ApiException::class.java)
            val idToken = account.idToken

            if (idToken.isNullOrBlank()) {
                onFailure("Google sign-in did not return an ID token.")
                return
            }

            val credential = GoogleAuthProvider.getCredential(idToken, null)

            auth.signInWithCredential(credential)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure(task.exception?.localizedMessage ?: "Firebase auth failed")
                    }
                }
        } catch (e: Exception) {
            onFailure(e.localizedMessage ?: "Google sign-in failed")
        }
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun logout() {
        auth.signOut()
    }
}
