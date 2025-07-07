package com.serenity

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.com.serenity.data.repository.AuthRepository
import java.com.serenity.data.JournalDao
import com.serenity.data.ChatSessionDao
import kotlinx.coroutines.tasks.await
import timber.log.Timber

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val backupService: BackupService,
    private val authRepository: AuthRepository,
    private val journalDao: JournalDao,
    private val chatSessionDao: ChatSessionDao
) : ViewModel() {

    private val _user = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    private val _signInState = MutableStateFlow<SignInState>(SignInState.Idle)
    val signInState: StateFlow<SignInState> = _signInState.asStateFlow()

    private val _backupState = MutableStateFlow<BackupState?>(null)
    val backupState: StateFlow<BackupState?> = _backupState.asStateFlow()

    init {
        // Listen for auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            _user.value = firebaseAuth.currentUser
            if (firebaseAuth.currentUser != null) {
                checkForBackup()
            }
        }
    }

    fun checkUser() {
        _user.value = auth.currentUser
        if (auth.currentUser != null) {
            checkForBackup()
        }
    }

    fun check() {
        if (authRepository.getCurrentUser() != null) {
            _signInState.value = SignInState.Success
        }
    }

    /**
     * Start Google Sign-In using Credential Manager
     */
    fun startGoogleSignIn(context: Context) {
        viewModelScope.launch {
            _signInState.value = SignInState.Loading
            try {
                val googleCredential = authRepository.getGoogleSignInCredential(context)
                if (googleCredential != null) {
                    val result = authRepository.signInWithGoogleCredential(googleCredential)
                    result.onSuccess {
                        _signInState.value = SignInState.Success
                    }.onFailure { ex ->
                        _signInState.value = SignInState.Error(ex.localizedMessage ?: "Sign-in failed")
                    }
                } else {
                    _signInState.value = SignInState.Error("Sign-in cancelled or failed")
                }
            } catch (e: Exception) {
                _signInState.value = SignInState.Error(e.localizedMessage ?: "Sign-in error")
            }
        }
    }

    /**
     * Legacy method for backward compatibility (if you still want to support ID token sign-in)
     */
    fun signInWithGoogleIdToken(idToken: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                _signInState.value = SignInState.Loading
                val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).await()
                _signInState.value = SignInState.Success
                onResult(true, null)
            } catch (e: Exception) {
                _signInState.value = SignInState.Error(e.localizedMessage ?: "Sign-in failed")
                onResult(false, e.localizedMessage)
            }
        }
    }

    fun getProfile(): FirebaseUser? = user.value

    fun signOut() {
        viewModelScope.launch {
            try {
                clearAllLocalData()
                auth.signOut()
                _user.value = null
                _signInState.value = SignInState.Idle
                _backupState.value = null
            } catch (e: Exception) {
                Timber.e(e, "Error during sign out")
            }
        }
    }

    private suspend fun clearAllLocalData() {
        try {
            journalDao.deleteAll()
            chatSessionDao.deleteAll()
            // Add more data clearing if needed
        } catch (e: Exception) {
            Timber.e(e, "Error clearing local data")
        }
    }

    private fun checkForBackup() {
        viewModelScope.launch {
            try {
                val lastBackupTime = backupService.getLastBackupTime()
                if (lastBackupTime.isSuccess && lastBackupTime.getOrNull() ?: 0L > 0) {
                    _backupState.value = BackupState.BackupAvailable(lastBackupTime.getOrNull() ?: 0L)
                }
            } catch (e: Exception) {
                // Silently fail - backup check is not critical
            }
        }
    }

    fun clearSignInState() {
        _signInState.value = SignInState.Idle
    }

    fun clearBackupState() {
        _backupState.value = null
    }
}

sealed class SignInState {
    object Idle : SignInState()
    object Loading : SignInState()
    object Success : SignInState()
    data class Error(val message: String) : SignInState()
}

sealed class BackupState {
    object Loading : BackupState()
    data class Success(val message: String) : BackupState()
    data class Error(val message: String) : BackupState()
    data class BackupAvailable(val lastBackupTime: Long) : BackupState()
}
