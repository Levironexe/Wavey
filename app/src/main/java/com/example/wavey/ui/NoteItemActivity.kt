package com.example.wavey.ui

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.wavey.DialogHelper
import com.example.wavey.R
import com.example.wavey.databinding.ActivityNoteItemBinding
import com.example.wavey.databinding.ActivityTaskItemBinding
import com.example.wavey.model.Note
import com.example.wavey.model.Task
import com.example.wavey.repository.NoteRepository
import com.example.wavey.repository.TaskRepository
import com.google.android.material.chip.Chip
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteItemBinding
    private var isEditMode = false
    private var noteId: String = ""
    private var userId: String = ""
    private var note: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Get task data from intent
        noteId = intent.getStringExtra("NOTE_ID") ?: ""
        userId = intent.getStringExtra("USER_ID")?: ""
        val noteTitle = intent.getStringExtra("NOTE_TITLE") ?: ""
        val noteDetails = intent.getStringExtra("NOTE_DETAILS") ?: ""
        val noteTimestamp = intent.getLongExtra("TASK_TIMESTAMP", System.currentTimeMillis())

        note = Note(
            id = noteId,
            title = noteTitle,
            details = noteDetails,
            creationDate = Timestamp(noteTimestamp / 1000, 0),
            userId = userId
        )

        // Populate views with task data
        binding.taskTitleEditText.setText(noteTitle)
        binding.taskDetailsEditText.setText(noteDetails)

        // Format and display creation date
        val date = Date(noteTimestamp)
        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        binding.creationDateText.text = sdf.format(date)

        // Set up click listeners
        setupClickListeners()
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
                "Are you sure you want to delete this note?",
                onDeleteConfirm = {
                    note?.let { it1 -> delete(it1.id) }
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            )
        }

    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
//        binding.saveButton.isEnabled = true
        val currentNote = note

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
        val currentTask = note ?: return

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
        val updateNote = Note(
            id = noteId,
            title = updatedTitle,
            details = updatedDetails,
            // Keep other fields the same as they're not editable in this UI
            creationDate = note?.creationDate ?: Timestamp.now(),
        )

        // Call your save method
        save(updateNote)

        // Exit edit mode
        toggleEditMode()
    }

    // This is where you'll implement your save logic
    private fun save(updateNote: Note) {
        val repository = NoteRepository()

        lifecycleScope.launch {
            try {
                val result = repository.updateNote(updateNote, updateNote.id)
                if (result.isSuccess) {
                    Toast.makeText(applicationContext, "Task is edited successfully", Toast.LENGTH_SHORT).show()
                } else  {
                    Toast.makeText(applicationContext, "Failed to add task", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        note = updateNote
    }

    private fun delete(noteId: String) {
        val repository = NoteRepository()
        lifecycleScope.launch {
            try {
                val result = repository.deleteNote(noteId)
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