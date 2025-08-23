package com.serenity.data.repository

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.com.serenity.BuildConfig
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {

    companion object {
        private const val TAG = "AuthRepository"
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    /**
     * Try One Tap (Credential Manager)
     */
    suspend fun getGoogleSignInCredential(context: Context): GoogleIdTokenCredential? {
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
            .setFilterByAuthorizedAccounts(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            Log.d(TAG, "Attempting One Tap sign-in with clientId=${BuildConfig.GOOGLE_CLIENT_ID}")
            val response: GetCredentialResponse = credentialManager.getCredential(
                request = request,
                context = context
            )
            Log.d(TAG, "CredentialManager response: type=${response.credential?.type}, data=${response.credential?.data}")
            GoogleIdTokenCredential.createFrom(response.credential.data)
        } catch (e: GetCredentialException) {
            Log.e(TAG, "One Tap failed: ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected One Tap error", e)
            null
        }
    }

    /**
     * Fallback: normal Google Sign-In intent
     */
    fun getGoogleSignInIntent(context: Context): Intent {
        Log.d(TAG, "Building GoogleSignInIntent with clientId=${BuildConfig.GOOGLE_CLIENT_ID}")

        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
            com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
            .requestEmail()
            .build()

        val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
        return googleSignInClient.signInIntent
    }

    /**
     * Sign in with Firebase
     */
    suspend fun signInWithGoogleCredential(googleCredential: GoogleIdTokenCredential): Result<FirebaseUser> {
        val idToken = googleCredential.idToken
        Log.d(TAG, "Firebase sign-in attempt with Google idToken=${idToken?.take(15)}...")

        return try {
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            val user = authResult.user
            if (user != null) {
                Log.d(TAG, "Firebase sign-in success: uid=${user.uid}, email=${user.email}, name=${user.displayName}")
                Result.success(user)
            } else {
                Log.e(TAG, "Firebase sign-in failed: user is null")
                Result.failure(NullPointerException("FirebaseUser is null"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase sign-in failed with error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Debug utility
     */
    fun printCurrentUserDebug() {
        val user = auth.currentUser
        if (user == null) {
            Log.d(TAG, "No Firebase user signed in.")
        } else {
            Log.d(TAG, "Firebase currentUser: uid=${user.uid}, email=${user.email}, displayName=${user.displayName}")
        }
    }
}
