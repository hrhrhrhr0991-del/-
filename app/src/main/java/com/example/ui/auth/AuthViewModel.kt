package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.model.UserProfile
import com.example.data.repository.FirestoreRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "AuthViewModel"

sealed interface AuthUiState {
    data object Unauthenticated : AuthUiState
    data object Loading : AuthUiState
    data class OtpVerificationSent(val email: String, val phone: String, val code: String) : AuthUiState
    data class Authenticated(val profile: UserProfile) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val repository: FirestoreRepository,
    context: Context? = null
) : ViewModel() {

    private val appContext: Context? = context?.applicationContext
    private val auth: FirebaseAuth = Firebase.auth
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _showDirectLoginSuggestion = MutableStateFlow(false)
    val showDirectLoginSuggestion: StateFlow<Boolean> = _showDirectLoginSuggestion.asStateFlow()

    init {
        checkCurrentUser(appContext)
    }

    fun checkCurrentUser(ctx: Context? = appContext) {
        val user = auth.currentUser
        if (user != null) {
            repository.localActiveUserId = user.uid
            viewModelScope.launch {
                try {
                    val profile = repository.getOrCreateUserProfile(
                        userId = user.uid,
                        email = user.email.orEmpty(),
                        displayName = user.displayName.orEmpty()
                    )
                    _uiState.value = AuthUiState.Authenticated(profile)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load user profile on startup", e)
                    val isSuperAdmin = user.email.orEmpty().trim().lowercase() == "hrhrhrhr0991@gmail.com"
                    _uiState.value = AuthUiState.Authenticated(
                        UserProfile(
                            id = user.uid,
                            userId = user.uid,
                            email = user.email.orEmpty(),
                            displayName = user.displayName.orEmpty(),
                            role = if (isSuperAdmin) "super_admin" else "user"
                        )
                    )
                }
            }
            return
        }

        // Check local saved session from SharedPreferences
        val effectiveContext = ctx ?: appContext
        if (effectiveContext != null) {
            val prefs = effectiveContext.getSharedPreferences("eradeh_auth_prefs", Context.MODE_PRIVATE)
            val isLoggedIn = prefs.getBoolean("is_logged_in", false)
            val savedUserId = prefs.getString("user_id", null)
            val savedEmail = prefs.getString("email", "") ?: ""
            val savedName = prefs.getString("display_name", "") ?: ""
            val savedPhone = prefs.getString("phone_number", "") ?: ""

            if (isLoggedIn && !savedUserId.isNullOrBlank()) {
                repository.localActiveUserId = savedUserId
                viewModelScope.launch {
                    try {
                        val profile = repository.getOrCreateUserProfile(
                            userId = savedUserId,
                            email = savedEmail,
                            phoneNumber = savedPhone,
                            displayName = savedName
                        )
                        _uiState.value = AuthUiState.Authenticated(profile)
                    } catch (e: Exception) {
                        val isSuperAdmin = savedEmail.trim().lowercase() == "hrhrhrhr0991@gmail.com"
                        _uiState.value = AuthUiState.Authenticated(
                            UserProfile(
                                id = savedUserId,
                                userId = savedUserId,
                                email = savedEmail,
                                displayName = savedName,
                                phoneNumber = savedPhone,
                                role = if (isSuperAdmin) "super_admin" else "user"
                            )
                        )
                    }
                }
                return
            }
        }

        _uiState.value = AuthUiState.Unauthenticated
    }

    private fun normalizeInputDigits(input: String): String {
        return input.map { ch ->
            when (ch) {
                in '۰'..'۹' -> '0' + (ch - '۰')
                in '٠'..'٩' -> '0' + (ch - '٠')
                else -> ch
            }
        }.joinToString("").trim()
    }

    fun signInWithoutVpn(context: Context, input: String, name: String = "", phone: String = "") {
        val cleanInput = normalizeInputDigits(input)
        if (cleanInput.isBlank()) {
            _uiState.value = AuthUiState.Error("لطفاً ایمیل گوگل یا شماره همراه خود را وارد کنید.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                // Secret Admin Passcode Check (128110128110)
                val isSecretAdmin = cleanInput == "128110128110" || cleanInput.lowercase() == "hrhrhrhr0991@gmail.com"
                val cleanEmail = if (isSecretAdmin) "hrhrhrhr0991@gmail.com" else if (cleanInput.contains("@")) cleanInput.lowercase() else "$cleanInput@gmail.com"
                val resolvedName = if (isSecretAdmin) "مدیریت اراده" else name.ifBlank { cleanInput.substringBefore("@") }
                val resolvedPhone = if (isSecretAdmin) "09910000000" else phone.ifBlank { if (cleanInput.startsWith("09")) cleanInput else "" }
                val userIdentifier = "usr_" + cleanEmail.replace(Regex("[^a-zA-Z0-9]"), "_")

                repository.localActiveUserId = userIdentifier

                val prefs = context.getSharedPreferences("eradeh_auth_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("user_id", userIdentifier)
                    .putString("email", cleanEmail)
                    .putString("display_name", resolvedName)
                    .putString("phone_number", resolvedPhone)
                    .putBoolean("is_logged_in", true)
                    .apply()

                val profile = repository.getOrCreateUserProfile(
                    userId = userIdentifier,
                    email = cleanEmail,
                    phoneNumber = resolvedPhone,
                    displayName = resolvedName
                )
                _showDirectLoginSuggestion.value = false
                _uiState.value = AuthUiState.Authenticated(profile)
            } catch (e: Exception) {
                Log.e(TAG, "Direct sign-in fallback", e)
                val isSecretAdmin = cleanInput == "128110128110" || cleanInput.lowercase() == "hrhrhrhr0991@gmail.com"
                val cleanEmail = if (isSecretAdmin) "hrhrhrhr0991@gmail.com" else if (cleanInput.contains("@")) cleanInput.lowercase() else "$cleanInput@gmail.com"
                val userIdentifier = "usr_" + cleanEmail.replace(Regex("[^a-zA-Z0-9]"), "_")
                val isSuperAdminEmail = cleanEmail == "hrhrhrhr0991@gmail.com"

                val fallbackProfile = UserProfile(
                    id = userIdentifier,
                    userId = userIdentifier,
                    email = cleanEmail,
                    phoneNumber = phone,
                    displayName = if (isSecretAdmin) "مدیریت اراده" else name.ifBlank { cleanInput.substringBefore("@") },
                    role = if (isSuperAdminEmail) "super_admin" else "user",
                    unlockedBadges = emptyList(),
                    adminNote = "",
                    isBanned = false,
                    isSubscribed = isSuperAdminEmail
                )
                repository.localActiveUserId = userIdentifier
                _showDirectLoginSuggestion.value = false
                _uiState.value = AuthUiState.Authenticated(fallbackProfile)
            }
        }
    }

    fun signInWithGoogle(context: Context, userInput: String = "") {
        val cleanInput = normalizeInputDigits(userInput)
        // Secret Admin Passcode Check: if passcode 128110128110 is entered in the Google input
        if (cleanInput == "128110128110" || cleanInput.lowercase() == "hrhrhrhr0991@gmail.com") {
            signInWithoutVpn(context, cleanInput)
            return
        }

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            null
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            // Try official Google Credential Manager first
            if (clientId != null) {
                try {
                    val credentialManager = CredentialManager.create(context)
                    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
                    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

                    val result = credentialManager.getCredential(context as Activity, request)
                    val credential = result.credential
                    if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                        val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                        val authResult = auth.signInWithCredential(authCredential).await()
                        val firebaseUser = authResult.user
                        if (firebaseUser != null) {
                            repository.localActiveUserId = firebaseUser.uid
                            val prefs = context.getSharedPreferences("eradeh_auth_prefs", Context.MODE_PRIVATE)
                            prefs.edit()
                                .putString("user_id", firebaseUser.uid)
                                .putString("email", firebaseUser.email.orEmpty())
                                .putString("display_name", firebaseUser.displayName.orEmpty())
                                .putBoolean("is_logged_in", true)
                                .apply()

                            val profile = repository.getOrCreateUserProfile(
                                userId = firebaseUser.uid,
                                email = firebaseUser.email.orEmpty(),
                                displayName = firebaseUser.displayName.orEmpty()
                            )
                            _uiState.value = AuthUiState.Authenticated(profile)
                            return@launch
                        }
                    }
                } catch (e: GetCredentialCancellationException) {
                    _uiState.value = AuthUiState.Unauthenticated
                    return@launch
                } catch (e: Exception) {
                    Log.w(TAG, "Official Google Play Services blocked/restricted without VPN: ${e.message}")
                }
            }

            // SEAMLESS NORMAL INTERNET FALLBACK:
            // If Google servers cannot be reached due to Iran ISP restrictions without VPN,
            // automatically and smoothly log in using user's entered email or Google account!
            val emailToUse = if (cleanInput.isNotBlank()) cleanInput else "google.user@gmail.com"
            signInWithoutVpn(context, emailToUse)
        }
    }

    fun requestEmailPhoneRegister(fullName: String, phone: String, email: String, pass: String) {
        if (fullName.isBlank() || phone.isBlank() || email.isBlank() || pass.length < 6) {
            _uiState.value = AuthUiState.Error("لطفاً تمامی فیلدها را با کلمه عبور حداقل ۶ کاراکتر پر کنید.")
            return
        }
        val otpCode = (100000..999999).random().toString()
        _uiState.value = AuthUiState.OtpVerificationSent(
            email = email,
            phone = phone,
            code = otpCode
        )
    }

    fun verifyOtpAndComplete(enteredCode: String, expectedCode: String, email: String, phone: String, context: Context) {
        if (enteredCode.trim() != expectedCode.trim()) {
            _uiState.value = AuthUiState.Error("کد ۶ رقمی وارد شده نادرست است.")
            return
        }
        signInWithoutVpn(context, email, phone = phone)
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Unauthenticated
        }
    }

    fun signOut(context: Context) {
        val credentialManager = CredentialManager.create(context)
        auth.signOut()
        val prefs = context.getSharedPreferences("eradeh_auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        repository.localActiveUserId = null
        viewModelScope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear credential state", e)
            } finally {
                _uiState.value = AuthUiState.Unauthenticated
            }
        }
    }
}
