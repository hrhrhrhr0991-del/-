package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.data.ai.GeminiAiManager
import com.example.data.repository.FirestoreRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.AuthUiState
import com.example.ui.auth.AuthViewModel
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.EradehTheme
import com.example.ui.theme.NeonCyan
import com.example.util.configureAppCheck
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // MANDATORY: Configure App Check before any Compose UI, ViewModels, or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        val repository = FirestoreRepository(this)
        val aiManager = GeminiAiManager()
        val authViewModel = AuthViewModel(repository)

        setContent {
            EradehTheme {
                MainRoot(
                    repository = repository,
                    aiManager = aiManager,
                    authViewModel = authViewModel
                )
            }
        }
    }
}

@Composable
fun MainRoot(
    repository: FirestoreRepository,
    aiManager: GeminiAiManager,
    authViewModel: AuthViewModel
) {
    val authUiState by authViewModel.uiState.collectAsState()
    var currentFirebaseUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentFirebaseUser = auth.currentUser
            authViewModel.checkCurrentUser()
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    when (val state = authUiState) {
        is AuthUiState.Authenticated -> {
            AppNavigation(
                currentUserId = state.profile.userId.ifBlank { currentFirebaseUser?.uid ?: "user_default" },
                userProfile = state.profile,
                repository = repository,
                aiManager = aiManager,
                authViewModel = authViewModel
            )
        }
        is AuthUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonCyan)
            }
        }
        else -> {
            AuthScreen(
                authViewModel = authViewModel,
                uiState = state
            )
        }
    }
}
