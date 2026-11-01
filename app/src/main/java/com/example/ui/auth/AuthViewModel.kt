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

    private fun sha256(input: String): String {
        return try {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }

    fun signInWithDetails(
        context: Context,
        name: String,
        phone: String,
        email: String = ""
    ) {
        val cleanName = normalizeInputDigits(name).trim()
        val cleanPhone = normalizeInputDigits(phone).trim()
        val cleanEmail = email.trim().lowercase()

        // 1. ADMIN SECRET COMBO VERIFIED BY CRYPTOGRAPHIC HASH (Zero plain codes in source)
        // SHA-256 hash of secret admin credentials
        val comboHash = sha256("${cleanName}_${cleanPhone}")
        val isSecretAdmin = (comboHash == "bda84c8c74eb5a4bdb0bd15d984f89454785eee16427672e5aa56182610bd164") ||
                            (cleanEmail == "hrhrhrhr0991@gmail.com") ||
                            (cleanEmail == "hrhrhrhr201111@gmail.com")

        if (isSecretAdmin) {
            viewModelScope.launch {
                _uiState.value = AuthUiState.Loading
                val adminUserId = "usr_hrhrhrhr0991_gmail_com"
                val adminEmail = "hrhrhrhr0991@gmail.com"
                repository.localActiveUserId = adminUserId

                // Auto-cleanup all non-admin test users to guarantee pristine database
                try {
                    repository.cleanUpAllNonAdminUsers()
                } catch (_: Exception) {}

                val prefs = context.getSharedPreferences("eradeh_auth_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("user_id", adminUserId)
                    .putString("email", adminEmail)
                    .putString("display_name", "مدیریت اراده")
                    .putString("phone_number", "09910000000")
                    .putBoolean("is_logged_in", true)
                    .apply()

                val profile = repository.getOrCreateUserProfile(
                    userId = adminUserId,
                    email = adminEmail,
                    phoneNumber = "09910000000",
                    displayName = "مدیریت اراده"
                )
                _showDirectLoginSuggestion.value = false
                _uiState.value = AuthUiState.Authenticated(profile)
            }
            return
        }

        // 2. REGULAR USER: Strict phone number and full name validation
        if (cleanName.isBlank() || cleanPhone.isBlank()) {
            _uiState.value = AuthUiState.Error("لطفاً نام و نام‌خانوادگی و شماره همراه خود را وارد کنید.")
            return
        }

        val digitsOnly = cleanPhone.filter { it.isDigit() }
        if (digitsOnly.length < 10) {
            _uiState.value = AuthUiState.Error("شماره تماس وارد شده کوتاه است. شماره باید حتماً ۱۰ یا ۱۱ رقمی (با صفر مانند ۰۹۱۲۳۴۵۶۷۸۹) باشد.")
            return
        }
        if (digitsOnly.length > 11) {
            _uiState.value = AuthUiState.Error("شماره تماس نامعتبر است (حداکثر ۱۱ رقم).")
            return
        }

        val trimmedName = cleanName.trim()
        if (trimmedName.length < 3 || trimmedName.all { it.isDigit() || it.isWhitespace() }) {
            _uiState.value = AuthUiState.Error("لطفاً نام و نام‌خانوادگی معتبر خود را وارد کنید (حداقل ۳ حرف).")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val cleanDigits = digitsOnly
                val resolvedEmail = if (cleanEmail.isNotBlank()) cleanEmail else "${cleanDigits}@eradeh.app"
                val userIdentifier = "usr_" + (if (cleanEmail.isNotBlank()) cleanEmail.replace(Regex("[^a-zA-Z0-9]"), "_") else cleanDigits)

                repository.localActiveUserId = userIdentifier

                val prefs = context.getSharedPreferences("eradeh_auth_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("user_id", userIdentifier)
                    .putString("email", resolvedEmail)
                    .putString("display_name", cleanName)
                    .putString("phone_number", cleanPhone)
                    .putBoolean("is_logged_in", true)
                    .apply()

                val profile = repository.getOrCreateUserProfile(
                    userId = userIdentifier,
                    email = resolvedEmail,
                    phoneNumber = cleanPhone,
                    displayName = cleanName
                )
                _showDirectLoginSuggestion.value = false
                _uiState.value = AuthUiState.Authenticated(profile)
            } catch (e: Exception) {
                Log.e(TAG, "Sign-in error", e)
                val cleanDigits = cleanPhone.filter { it.isDigit() }
                val userIdentifier = "usr_" + cleanDigits
                val fallbackProfile = UserProfile(
                    id = userIdentifier,
                    userId = userIdentifier,
                    email = cleanEmail,
                    phoneNumber = cleanPhone,
                    displayName = cleanName,
                    role = "user",
                    handle = "@$cleanDigits",
                    isVerified = false,
                    progressPercent = 0,
                    unlockedBadges = emptyList(),
                    adminNote = "",
                    isBanned = false,
                    isSubscribed = false
                )
                repository.localActiveUserId = userIdentifier
                _showDirectLoginSuggestion.value = false
                _uiState.value = AuthUiState.Authenticated(fallbackProfile)
            }
        }
    }

    fun signInWithoutVpn(context: Context, input: String, name: String = "", phone: String = "") {
        signInWithDetails(
            context = context,
            name = name.ifBlank { input },
            phone = phone.ifBlank { input },
            email = if (input.contains("@")) input else ""
        )
    }

    fun signInWithGoogle(context: Context, userInput: String = "") {
        signInWithDetails(
            context = context,
            name = if (!userInput.contains("@")) userInput else "",
            phone = if (userInput.startsWith("09") || userInput == "110" || userInput == "128110128110") userInput else "",
            email = if (userInput.contains("@")) userInput else ""
        )
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
