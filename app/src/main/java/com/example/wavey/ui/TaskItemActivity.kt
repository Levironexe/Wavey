package com.example.wavey.ui

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.wavey.DialogHelper
import com.example.wavey.R
import com.example.wavey.databinding.ActivityTaskItemBinding
import com.example.wavey.model.Task
import com.example.wavey.repository.TaskRepository
import com.google.android.material.chip.Chip
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskItemBinding
    private var isEditMode = false
    private var taskId: String = ""
    private var userId: String = ""
    private var task: Task? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get task data from intent
        taskId = intent.getStringExtra("TASK_ID") ?: ""
        userId = intent.getStringExtra("USER_ID")?: ""
        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: ""
        val taskDetails = intent.getStringExtra("TASK_DETAILS") ?: ""
        val isCompleted = intent.getBooleanExtra("TASK_IS_COMPLETED", false)
        val taskTimestamp = intent.getLongExtra("TASK_TIMESTAMP", System.currentTimeMillis())
        val taskDueDate = intent.getLongExtra("DUE_DATE", System.currentTimeMillis())
        val taskTags = intent.getStringArrayListExtra("TASK_TAGS") ?: arrayListOf()

        task = Task(
            id = taskId,
            title = taskTitle,
            details = taskDetails,
            completed = isCompleted,
            timestamp = Timestamp(taskTimestamp / 1000, 0),
            dueDate = Timestamp(taskDueDate / 1000, 0),
            tags = taskTags,
            userId = userId
        )

        // Populate views with task data
        binding.taskTitleEditText.setText(taskTitle)
        binding.taskDetailsEditText.setText(taskDetails)

        // Format and display creation date
        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

        val date = Date(taskTimestamp)
        binding.creationDateText.text = sdf.format(date)

        val dueDate = Date(taskDueDate)
        binding.dueDateText.text = sdf.format(dueDate)

        // Add tags to chip group
        setupTagChips(taskTags)

        // Set up click listeners
        setupClickListeners()
    }

    private fun setupTagChips(tags: List<String>) {
        binding.chipGroupTags.removeAllViews()

        for (tag in tags) {
            val chip = Chip(this)
            chip.text = tag
            chip.isCheckable = false
            chip.isClickable = false

            // Apply color based on tag name
            val colorId = when (tag) {
                "work" -> R.color.very_light_yellow
                "school" -> R.color.very_light_green
                "errands" -> R.color.very_light_blue
                "shopping" -> R.color.very_light_purple
                "finance" -> R.color.very_light_pink
                else -> R.color.very_light_red
            }

            chip.chipBackgroundColor = getColorStateList(colorId)
            chip.setTextColor(getColor(R.color.black))
            binding.chipGroupTags.addView(chip)
        }

        if (tags.isEmpty()) {
            binding.tvNoTags.visibility = View.VISIBLE
        } else {
            binding.tvNoTags.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        // Back button
        binding.backButton.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }

        // Edit button
        binding.editButton.setOnClickListener {
            toggleEditMode()
        }

        // Save button
        binding.saveButton.setOnClickListener {
            saveChanges()
        }

        // Delete button
        binding.deleteButton.setOnClickListener{
            DialogHelper.deleteDialog(
                this,
                "Are you sure you want to delete this task?",
                onDeleteConfirm = {
                    task?.let { it1 -> delete(it1.id) }
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            )
        }

    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
//        binding.saveButton.isEnabled = true
        val currentTask = task

        // Toggle editability of fields
        binding.taskTitleEditText.isEnabled = isEditMode
        binding.taskDetailsEditText.isEnabled = isEditMode


        if (isEditMode) {
            setupTextChangeListeners()
            updateSaveButtonState()
        }
        // Show/hide save button
        binding.saveButton.visibility = if (isEditMode) View.VISIBLE else View.GONE



        // Change edit button icon
        binding.editButton.setImageResource(
            if (isEditMode) R.drawable.ic_pencil_off else R.drawable.ic_pencil
        )
    }
    private fun setupTextChangeListeners() {
        // Only set up the listeners once
        val textWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: android.text.Editable?) {
                updateSaveButtonState()
            }
        }

        // Remove existing listeners first to avoid duplicates
        binding.taskTitleEditText.removeTextChangedListener(textWatcher)
        binding.taskDetailsEditText.removeTextChangedListener(textWatcher)

        // Add the listeners
        binding.taskTitleEditText.addTextChangedListener(textWatcher)
        binding.taskDetailsEditText.addTextChangedListener(textWatcher)
    }

    private fun updateSaveButtonState() {
        val currentTask = task ?: return

        val currentTitle = binding.taskTitleEditText.text.toString()
        val currentDetails = binding.taskDetailsEditText.text.toString()

        // Enable save button only if content has changed from original task
        val hasChanged = (currentTitle != currentTask.title ||
                currentDetails != currentTask.details)

        binding.saveButton.isEnabled = hasChanged && currentTitle.isNotEmpty()
    }

    private fun saveChanges() {
        // Get updated values
        val updatedTitle = binding.taskTitleEditText.text.toString().trim()
        val updatedDetails = binding.taskDetailsEditText.text.toString().trim()

        // Validate input
        if (updatedTitle.isEmpty()) {
            binding.titleInputLayout.error = "Title cannot be empty"
            return
        }
        else {
            binding.titleInputLayout.error = null
        }

        // Create updated task object
        val updatedTask = Task(
            id = taskId,
            title = updatedTitle,
            details = updatedDetails,
            timestamp = task?.timestamp ?: Timestamp.now(),
            tags = task?.tags ?: listOf(),
        )

        // Call your save method
        save(updatedTask)

        // Exit edit mode
        toggleEditMode()
    }

    // This is where you'll implement your save logic
    private fun save(updatedTask: Task) {
        val repository = TaskRepository()

        lifecycleScope.launch {
            try {
                val result = repository.updateTask(updatedTask, updatedTask.id)
                if (result.isSuccess) {
                    Toast.makeText(applicationContext, "Task is edited successfully", Toast.LENGTH_SHORT).show()
                } else  {
                    Toast.makeText(applicationContext, "Failed to edit task", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        // This could involve calling your ViewModel or Repository
        // Update the local task reference
        task = updatedTask
    }

    private fun delete(taskId: String) {
        val repository = TaskRepository()
        lifecycleScope.launch {
            try {
                val result = repository.deleteTask(taskId)
                if (result.isSuccess) {
                    Toast.makeText(applicationContext, "Task is deleted", Toast.LENGTH_SHORT).show()
                } else  {
                    Toast.makeText(applicationContext, "Failed to delete task", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}