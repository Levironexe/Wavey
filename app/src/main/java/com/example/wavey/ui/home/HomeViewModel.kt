package com.example.wavey.ui.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavey.model.Task
import com.example.wavey.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repository = TaskRepository()

    // For task list
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    // For upcoming tasks
    private val _upcomingTasks = MutableStateFlow<List<Task>>(emptyList())
    val upcomingTasks: StateFlow<List<Task>> = _upcomingTasks.asStateFlow()

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
    fun loadTasks() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = repository.getTasks()

                if (result.isSuccess) {
                    _tasks.value = result.getOrNull() ?: emptyList()
                } else {
                    _error.value = "Failed to load tasks: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUpcomingTasks() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = repository.getUpComingTasks()
                if (result.isSuccess) {
                    _upcomingTasks.value = result.getOrNull() ?: emptyList()
                } else {
                    _error.value = "Failed to load upcoming tasks: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _error.value = "Error loading upcoming tasks: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Add a new task
    fun addTask(task: Task) {
        viewModelScope.launch {
            try {
                val result = repository.addTask(task)
                _addTaskResult.value = result

                // Reload tasks if successful to update the list
                if (result.isSuccess) {
                    loadTasks()
                }
            } catch (e: Exception) {
                _addTaskResult.value = Result.failure(e)
            }
        }
    }

    // Toggle task completion status
    fun toggleTaskCompletion(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                val result = repository.toggleTaskCompletion(taskId, isCompleted)

                if (result.isSuccess) {
                    // Update the task in the current list
                    _tasks.value = _tasks.value.map { task ->
                        if (task.id == taskId) task.copy(completed = isCompleted) else task
                    }
                } else {
                    _error.value = "Failed to update task: ${result.exceptionOrNull()?.message}"
                    loadTasks() // Reload to get correct state
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                loadTasks() // Reload to get correct state
            }
        }
    }

    // Delete a task
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                val result = repository.deleteTask(taskId)
                if (result.isSuccess) {
                    // Remove task from current list
                    _tasks.value = _tasks.value.filter { it.id != taskId }
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