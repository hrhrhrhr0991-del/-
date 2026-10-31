package com.example.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserNote
import com.example.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface NotesUiState {
    data object Loading : NotesUiState
    data class Success(val notes: List<UserNote>) : NotesUiState
    data class Error(val message: String) : NotesUiState
}

class NotesViewModel(
    private val repository: FirestoreRepository
) : ViewModel() {

    val notesState: StateFlow<NotesUiState> = repository.observeNotes()
        .map<List<UserNote>, NotesUiState> { NotesUiState.Success(it) }
        .catch { emit(NotesUiState.Error(it.localizedMessage ?: "خطا در دریافت یادداشت‌ها")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = NotesUiState.Loading
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("همه")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun addNote(title: String, content: String, category: String, priority: String) {
        viewModelScope.launch {
            try {
                repository.addNote(title, content, category, priority)
            } catch (e: Exception) {
                // handled by repository
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            try {
                repository.deleteNote(noteId)
            } catch (e: Exception) {
                // handled
            }
        }
    }
}
