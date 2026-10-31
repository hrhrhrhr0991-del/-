package com.example.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.RoutineTask
import com.example.data.model.UserProfile
import com.example.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface RoutinesUiState {
    data object Loading : RoutinesUiState
    data class Success(val tasks: List<RoutineTask>) : RoutinesUiState
    data class Error(val message: String) : RoutinesUiState
}

class DashboardViewModel(
    private val repository: FirestoreRepository
) : ViewModel() {

    val routinesState: StateFlow<RoutinesUiState> = repository.observeRoutines()
        .map<List<RoutineTask>, RoutinesUiState> { RoutinesUiState.Success(it) }
        .catch { emit(RoutinesUiState.Error(it.localizedMessage ?: "خطا در دریافت لیست تسک‌ها")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = RoutinesUiState.Loading
        )

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            repository.observeUserProfile(userId).collect {
                _userProfile.value = it
            }
        }
    }

    fun toggleTask(taskId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            try {
                repository.updateRoutineCompletion(taskId, !currentStatus)
            } catch (e: Exception) {
                // handled by repository logging
            }
        }
    }

    fun addNewRoutine(
        title: String,
        type: String,
        time: String,
        strict: Boolean,
        description: String = "",
        category: String = "صبحگاهی",
        priority: String = "high",
        repeatDays: String = "هر روز"
    ) {
        viewModelScope.launch {
            try {
                repository.addRoutine(
                    RoutineTask(
                        title = title,
                        type = type,
                        targetTime = time,
                        strictLockEnabled = strict,
                        description = description,
                        category = category,
                        priority = priority,
                        repeatDays = repeatDays
                    )
                )
            } catch (e: Exception) {
                // handled
            }
        }
    }

    fun updateUserPersona(archetype: String, tone: String) {
        viewModelScope.launch {
            try {
                repository.updateUserPersona(archetype, tone)
            } catch (e: Exception) {
                // handled
            }
        }
    }

    fun deleteRoutine(taskId: String) {
        viewModelScope.launch {
            try {
                repository.deleteRoutine(taskId)
            } catch (e: Exception) {
                // handled
            }
        }
    }
}
