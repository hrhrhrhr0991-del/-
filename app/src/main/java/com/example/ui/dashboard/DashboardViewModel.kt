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

    // Default water intake starts at 0 (صفر صفر) until user records glasses
    private val _waterGlasses = MutableStateFlow(0)
    val waterGlasses: StateFlow<Int> = _waterGlasses.asStateFlow()

    fun setWaterGlasses(count: Int) {
        _waterGlasses.value = count.coerceIn(0, 8)
    }

    fun seedGoldenInternationalRoutines() {
        viewModelScope.launch {
            val goldenList = listOf(
                RoutineTask(
                    title = "🌅 پروتکل سحرخیزی و تنفس ۴-۴-۴-۴",
                    type = "wake_voice",
                    targetTime = "06:30",
                    strictLockEnabled = true,
                    description = "تنفس جعبه‌ای ریتمیک جهت اکسیژن‌رسانی به مغز و پاکسازی استرس اول صبح",
                    category = "صبحگاهی",
                    priority = "high",
                    repeatDays = "هر روز"
                ),
                RoutineTask(
                    title = "🛏️ اسکن تصویری هوشمند تختخواب",
                    type = "bed_inspection",
                    targetTime = "07:00",
                    strictLockEnabled = true,
                    description = "تأیید تصویری اولین پیروزی روز توسط هوش مصنوعی بینایی ماشین",
                    category = "صبحگاهی",
                    priority = "high",
                    repeatDays = "هر روز"
                ),
                RoutineTask(
                    title = "💧 آبرسانی هوشمند (۸ لیوان / ۲۰۰۰ سی‌سی)",
                    type = "standard",
                    targetTime = "07:30",
                    strictLockEnabled = false,
                    description = "رساندن آب کافی به بدن جهت فعال‌سازی متابولیسم و افزایش ۳۰ درصدی تمرکز",
                    category = "سلامتی",
                    priority = "medium",
                    repeatDays = "هر روز"
                ),
                RoutineTask(
                    title = "⚡ بلاک تمرکز عمیق سایبر (۴۵ دقیقه)",
                    type = "standard",
                    targetTime = "09:00",
                    strictLockEnabled = true,
                    description = "اجرای کار عمیق و پرچالش بدون چک کردن شبکه‌های اجتماعی و نوتیفیکیشن‌ها",
                    category = "تمرکز",
                    priority = "high",
                    repeatDays = "هر روز"
                ),
                RoutineTask(
                    title = "🧠 تمرین صوتی و بازخوانی بیانیه اراده",
                    type = "wake_voice",
                    targetTime = "13:00",
                    strictLockEnabled = false,
                    description = "تثبیت خودکارآمدی و قدرت تصمیم‌گیری با هوش مصنوعی صوتی",
                    category = "انگیزشی",
                    priority = "medium",
                    repeatDays = "هر روز"
                ),
                RoutineTask(
                    title = "📖 بازتاب روزانه و ثبت شکرگزاری در دفترچه",
                    type = "standard",
                    targetTime = "22:30",
                    strictLockEnabled = false,
                    description = "مرور دستاوردهای روز و بستن پرونده‌های ذهنی قبل از خواب آرام",
                    category = "شبانگاهی",
                    priority = "high",
                    repeatDays = "هر روز"
                )
            )

            for (task in goldenList) {
                try {
                    repository.addRoutine(task)
                } catch (e: Exception) {
                    // ignore
                }
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
