package java.com.serenity.data.repository

import android.content.Context
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
import timber.log.Timber
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun getGoogleSignInCredential(context: Context): GoogleIdTokenCredential? {
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId("807530362982-82d4366p3pa3kpqpme61u0231mkt0alb.apps.googleusercontent.com")
            .setFilterByAuthorizedAccounts(false)
            .setFilterByAuthorizedAccounts(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val response: GetCredentialResponse = credentialManager.getCredential(
                request = request,
                context = context
            )
            // Extract GoogleIdTokenCredential from the response
            GoogleIdTokenCredential.createFrom(response.credential.data)
        } catch (e: GetCredentialException) {
            Timber.e(e, "Credential Manager: No credential found or user canceled")
            null
        } catch (e: Exception) {
            Timber.e(e, "Credential Manager: Unexpected error")
            null
        }
    }

    suspend fun signInWithGoogleCredential(googleCredential: GoogleIdTokenCredential): Result<FirebaseUser> {
        val idToken = googleCredential.idToken
        return try {
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            authResult.user?.let { Result.success(it) }
                ?: Result.failure(NullPointerException("FirebaseUser is null"))
        } catch (e: Exception) {
            Timber.e(e, "Firebase sign-in failed")
            Result.failure(e)
        }
    }
}
