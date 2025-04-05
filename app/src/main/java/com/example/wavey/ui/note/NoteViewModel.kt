package com.example.wavey.ui.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavey.model.Note
import com.example.wavey.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteViewModel : ViewModel() {
    private val repository = NoteRepository()

    // For task list
    private val _note = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _note.asStateFlow()

    // For loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // For error handling
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // For add task result
    private val _addTaskResult = MutableLiveData<Result<String>?>(null)
    val addTaskResult: LiveData<Result<String>?> = _addTaskResult

    // Load all tasks for the current user
    fun loadNotes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = repository.getNotes()

                if (result.isSuccess) {
                    _note.value = result.getOrNull() ?: emptyList()
                } else {
                    _error.value = "Failed to load notes: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Add a new task
    fun addNote(note: Note) {
        viewModelScope.launch {
            try {
                val result = repository.addNote(note)
                _addTaskResult.value = result

                // Reload tasks if successful to update the list
                if (result.isSuccess) {
                    loadNotes()
                }
            } catch (e: Exception) {
                _addTaskResult.value = Result.failure(e)
            }
        }
    }


    // Delete a task
    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            try {
                val result = repository.deleteNote(noteId)
                if (result.isSuccess) {
                    // Remove task from current list
                    _note.value = _note.value.filter { it.id != noteId }
                } else {
                    _error.value = "Failed to delete task: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            }
        }
    }

    // Reset add task result
    fun resetAddTaskResult() {
        _addTaskResult.value = null
    }

    // Clear error
    fun clearError() {
        _error.value = null
    }
}