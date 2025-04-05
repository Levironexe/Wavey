package com.example.wavey.ui

import android.animation.AnimatorInflater
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.wavey.R
import com.example.wavey.databinding.ActivityMainBinding
import com.example.wavey.databinding.NoteFormBinding
import com.example.wavey.databinding.TaskFormBinding
import com.example.wavey.manager.slideFromRight
import com.example.wavey.model.Note
import com.example.wavey.repository.TaskRepository
import com.example.wavey.model.Task
import com.example.wavey.repository.NoteRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import jp.wasabeef.blurry.Blurry
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var createTaskBinding: TaskFormBinding
    private lateinit var createNoteBinding: NoteFormBinding
    private var currentBlurredView: ViewGroup? = null
    private lateinit var taskBottomSheetBehavior: BottomSheetBehavior<NestedScrollView>
    private lateinit var noteBottomSheetBehavior: BottomSheetBehavior<NestedScrollView>

    private var dueDate: Timestamp = Timestamp.now()
    private var userEmail: String = ""
    private var userProfilePictureUrlString: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if user is logged in before processing
        if (!isUserLoggedIn()) {
            redirectToLogin()
            return  // Stop further processing
        }

        // Process normal startup
        setupMainActivity(savedInstanceState)

        // Handle potential Assistant intent
        handleAssistantIntent(intent)
    }

    private fun setupMainActivity(savedInstanceState: Bundle?) {
        userEmail = intent.getStringExtra("EMAIL_OR_USERNAME") ?: "conmemay"
        userProfilePictureUrlString = intent.getStringExtra("PROFILE_PICTURE")
        val userProfilePictureUrl: Uri? = if (userProfilePictureUrlString != null) Uri.parse(userProfilePictureUrlString) else null

        supportActionBar?.hide()
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        binding.navView.findViewById<View>(R.id.navigation_home)?.applyNavButtonTouchEffect()
        binding.navView.findViewById<View>(R.id.navigation_tasks)?.applyNavButtonTouchEffect()
        binding.navView.findViewById<View>(R.id.navigation_note)?.applyNavButtonTouchEffect()

        binding.profileImage.setOnClickListener {
            val intent = Intent(applicationContext, ProfileActivity::class.java)
            intent.putExtra("PROFILE_PICTURE", userProfilePictureUrlString)
            intent.putExtra("EMAIL_OR_USERNAME", userEmail)
            startActivity(intent)
        }

        navView.setupWithNavController(navController)
        setupToolbar(userProfilePictureUrl, userEmail)
        setupFabMenuButton()
        setupBottomSheetTask()
        setupBottomSheetNote()
        setupTaskForm()
        setupNoteForm()
        addNote()
        addTask()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Check if user is logged in before processing any new intents
        if (!isUserLoggedIn()) {
            redirectToLogin()
            return
        }

        // Handle the intent from Google Assistant
        handleAssistantIntent(intent)
    }
    private fun isUserLoggedIn(): Boolean {
        // Implement your login check logic
        // For example, checking SharedPreferences or Firebase Auth
//        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
//        return sharedPreferences.getBoolean("is_logged_in", false)
        return true
        // If you're using Firebase Auth:
        // return FirebaseAuth.getInstance().currentUser != null
    }
    private fun redirectToLogin() {
        val loginIntent = Intent(this, LoginActivity::class.java)
        // Clear the back stack so users can't go back to MainActivity without logging in
        loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(loginIntent)
        finish()
    }

    private fun handleAssistantIntent(intent: Intent) {
        // Check if the app was launched from Google Assistant
        if (intent.action == "com.example.wavey.ASSISTANT_ACTION") {
            when {
                // Feature requests
                intent.hasExtra("feature") -> {
                    val feature = intent.getStringExtra("feature")
                    navigateByFeature(feature)
                }

                // Task actions
                intent.hasExtra("create_task") -> {
                    // Show the task creation interface
                    showBottomSheetTask()
                }

                intent.hasExtra("task_id") -> {
                    val taskId = intent.getStringExtra("task_id")
                    openTask(taskId)
                }

                // Note actions
                intent.hasExtra("create_note") -> {
                    // Show the note creation interface
                    showBottomSheetNote()
                }

                intent.hasExtra("note_id") -> {
                    val noteId = intent.getStringExtra("note_id")
                    openNote(noteId)
                }
            }
        }
    }

    private fun navigateByFeature(feature: String?) {
        // Navigate to the appropriate section based on the requested feature
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        when (feature) {
            "home" -> {
                navController.navigate(R.id.navigation_home)
            }
            "tasks", "task_list" -> {
                navController.navigate(R.id.navigation_tasks)
            }
            "notes", "note_list" -> {
                navController.navigate(R.id.navigation_note)
            }
            "profile" -> {
                val intent = Intent(applicationContext, ProfileActivity::class.java)
                intent.putExtra("PROFILE_PICTURE", userProfilePictureUrlString)
                intent.putExtra("EMAIL_OR_USERNAME", userEmail)
                startActivity(intent)
            }
            "add_task" -> {
                navController.navigate(R.id.navigation_tasks)
                showBottomSheetTask()
            }
            "add_note" -> {
                navController.navigate(R.id.navigation_note)
                showBottomSheetNote()
            }
        }
    }

    private fun openTask(taskId: String?) {
        if (taskId != null) {
            val intent = Intent(this, TaskItemActivity::class.java)
            intent.putExtra("TASK_ID", taskId)
            startActivity(intent)
        }
    }

    private fun openNote(noteId: String?) {
        if (noteId != null) {
            val intent = Intent(this, NoteItemActivity::class.java)
            intent.putExtra("NOTE_ID", noteId)
            startActivity(intent)
        }
    }
    private fun setupToolbar(userProfilePictureUrl: Uri? = null, userEmail: String) {
        // Set the toolbar as the action bar
        setSupportActionBar(binding.toolbar)
        // Remove default title
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Get current user information
        // Set user email
        binding.tvUserEmail.text = userEmail

        // Load profile image using Glide
        val profileImageUrl = userProfilePictureUrl
        if (profileImageUrl != null) {
            Glide.with(this)
                .load(profileImageUrl)
                .placeholder(R.drawable.ic_profile_default)
                .error(R.drawable.ic_profile_default)
                .into(binding.profileImage)
        } else {
            // Use default profile image if no photo URL exists
            binding.profileImage.setImageResource(R.drawable.ic_profile_default)
        }

    }
    private fun View.applyNavButtonTouchEffect() {
        setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    AnimatorInflater.loadAnimator(context, R.animator.nav_item_press).apply {
                        setTarget(v)
                        start()
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    AnimatorInflater.loadAnimator(context, R.animator.nav_item_release).apply {
                        setTarget(v)
                        start()
                    }
                    if (event.action == MotionEvent.ACTION_UP) v.performClick() // Ensure accessibility
                }
            }
            false // Allow normal item selection
        }
    }

    private fun setupFabMenuButton() {
        val addButton: MaterialButton = binding.actionButton
        var addButtonIsClick = false  // Initial state
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Track destination changes to update blur targeting
        navController.addOnDestinationChangedListener { _, _, _ ->
            // If menu is open when navigation happens, close it
            if (addButtonIsClick) {
                fabButtonCloseAnimation()
                closeMenu()
                addButtonIsClick = false
            }
        }

        addButton.setOnClickListener {
            addButtonIsClick = !addButtonIsClick

            if (addButtonIsClick) {
                // Rotate to -135 degrees (plus sign becomes an X)
                fabButtonOpenAnimation()
            } else {
                // Rotate back to 0 degrees
                fabButtonCloseAnimation()
            }
        }
        binding.root.setOnClickListener {
            fabButtonCloseAnimation()
        }
    }

    private fun addTask() {
        val addTaskButton: MaterialButton = binding.addTask
        var addTaskButtonIsClick = false  // Initial state
        addTaskButton.setOnClickListener {
            addTaskButtonIsClick = !addTaskButtonIsClick

            if (addTaskButtonIsClick) {
                showBottomSheetTask()
                fabButtonCloseAnimation()
                closeMenu()
                addTaskButtonIsClick = false

            }
        }
        createTaskBinding.closeButton.setOnClickListener{
            hideBottomSheetTask()
        }
    }

    private fun addNote() {
        val addNoteButton: MaterialButton = binding.addNote
        var addNoteButtonIsClick = false  // Initial state
        addNoteButton.setOnClickListener {
            addNoteButtonIsClick = !addNoteButtonIsClick

            if (addNoteButtonIsClick) {
                showBottomSheetNote()
                fabButtonCloseAnimation()
                closeMenu()
                addNoteButtonIsClick = false

            }
        }
        createNoteBinding.closeButton.setOnClickListener{
            hideBottomSheetNote()
        }
    }

    private fun fabButtonOpenAnimation() {
        binding.actionButton.animate()
            .rotation(-135f)
            .setDuration(300)
            .start()
        openMenu()
    }
    private fun fabButtonCloseAnimation() {
        binding.actionButton.animate()
            .rotation(0f)
            .setDuration(300)
            .start()
        closeMenu()
    }



    private fun openMenu() {
        // Show buttons first with animation
        binding.addTask.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).duration = 200
        }.slideFromRight(200L, 150L)
        binding.addNote.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).duration = 200
        }.slideFromRight(200L, 100L)

        // Apply blur based on current fragment
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val currentDestinationId = navController.currentDestination?.id

        try {
            // Find the appropriate view to blur
            val viewToBlur: ViewGroup? = when (currentDestinationId) {
                R.id.navigation_home -> findViewById<ViewGroup>(R.id.homeFragment)
                R.id.navigation_tasks -> findViewById<ViewGroup>(R.id.tasksFragment)
                R.id.navigation_note -> findViewById<ViewGroup>(R.id.noteFragment)
                else -> findViewById<ViewGroup>(R.id.nav_host_fragment_activity_main)
            }

            // Apply blur if we have a valid view
            viewToBlur?.let {
                currentBlurredView = it
                viewToBlur.post {
                    Blurry.with(this)
                        .radius(10)
                        .sampling(2)
                        .async()
                        .animate(100)
                        .onto(it)
                }
            }
        } catch (e: Exception) {
            // Fallback to blurring the entire content area
            val container = findViewById<ViewGroup>(R.id.MainActivity)
            currentBlurredView = container
            container.post {
                Blurry.with(this)
                    .radius(10)
                    .sampling(2)
                    .async()
                    .animate(100)
                    .onto(container)
            }
        }

        currentBlurredView?.setOnClickListener { _ ->
            if (binding.actionButton.rotation != 0f) {
                fabButtonCloseAnimation()
            }
        }
    }

    private fun closeMenu() {
        // First remove blur from current view (if any)
        currentBlurredView?.let {
            Blurry.delete(it)
            it.setOnClickListener(null)
        }
        currentBlurredView = null

        // Then hide buttons with animation
        binding.addTask.animate().alpha(0f).withEndAction {
            binding.addTask.visibility = View.GONE
            binding.addTask.alpha = 1f  // Reset alpha for next time
        }.duration = 200

        binding.addNote.animate().alpha(0f).withEndAction {
            binding.addNote.visibility = View.GONE
            binding.addNote.alpha = 1f
        }.duration = 200
    }

    override fun onDestroy() {
        super.onDestroy()
        currentBlurredView?.let {
            Blurry.delete(it)
        }
    }

    private fun setupBottomSheetTask() {
        // Get the bottom sheet view
        val bottomSheetView = findViewById<NestedScrollView>(R.id.bottomSheetTask)

        // Initialize BottomSheetBehavior
        taskBottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView)

        // Configure behavior
        taskBottomSheetBehavior.apply {
            isHideable = true
            state = BottomSheetBehavior.STATE_HIDDEN

            // Set half-expanded ratio (50% of screen)
            isFitToContents = false
            halfExpandedRatio = 0.6f

            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_HIDDEN -> {
                            hideBottomSheetTask()
                        }
                        BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                            binding.root.setOnClickListener() {
                                hideBottomSheetTask()
                            }
                        }
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            binding.root.setOnClickListener() {
                                hideBottomSheetTask()
                            }
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                }
            })
        }
        createTaskBinding = TaskFormBinding.bind(bottomSheetView)

    }

    private fun setupTaskForm() {
        val repository = TaskRepository()
        var showCalendar = false
        var showChip = false

        // Get save button reference
        val saveButton = createTaskBinding.btnSave
        val dateButton = createTaskBinding.btnDate
        val tagButton = createTaskBinding.btnTaskType

        dateButton.setOnClickListener{
            showCalendar = !showCalendar
            if (showCalendar) {
                createTaskBinding.calendarLayout.appCalendar.visibility = View.VISIBLE
                createTaskBinding.btnDate.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
                createTaskBinding.btnDate.setIconTintResource(R.color.black)
            } else {
                createTaskBinding.calendarLayout.appCalendar.visibility = View.GONE
                createTaskBinding.btnDate.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
                createTaskBinding.btnDate.setIconTintResource(R.color.white)
            }

        }

        tagButton.setOnClickListener{
            showChip = !showChip
            if (showChip) {
                createTaskBinding.tagDisplay.visibility = View.VISIBLE
                createTaskBinding.btnTaskType.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
                createTaskBinding.btnTaskType.setIconTintResource(R.color.black)
            } else {
                createTaskBinding.tagDisplay.visibility = View.GONE
                createTaskBinding.btnTaskType.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
                createTaskBinding.btnTaskType.setIconTintResource(R.color.white)
            }

        }


        createTaskBinding.calendarLayout.calendarView.setOnDateChangeListener{ _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val selectedDateInMillis = calendar.timeInMillis

            // Convert to Firebase Timestamp (if needed)
            val firebaseTimestamp = Timestamp(selectedDateInMillis / 1000, 0)
            dueDate = firebaseTimestamp
        }


        saveButton.setOnClickListener {
            // Get form values
            val title = createTaskBinding.etTaskTitle.text.toString().trim()
            val details = createTaskBinding.etDetails.text.toString().trim()


            // Validate input
            if (title.isEmpty()) {
                createTaskBinding.etTaskTitle.error = "Title is required"
                return@setOnClickListener
            }

            // Get selected tags
            val selectedTags = mutableListOf<String>()
            val chipGroup = createTaskBinding.chipGroupAttributes


            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i) as? com.google.android.material.chip.Chip
                if (chip?.isChecked == true) {
                    selectedTags.add(chip.text.toString())
                }
            }

            // Create Task object
            val task = Task(
                title = title,
                details = details,
                timestamp = Timestamp.now(),
                dueDate = dueDate.let { Timestamp(it.seconds, it.nanoseconds) },
                tags = selectedTags,
                completed = false,
            )


            hideBottomSheetTask()

            // Save to Firestore
            lifecycleScope.launch {
                try {
                    val result = repository.addTask(task)
                    if (result.isSuccess) {
                        // Clear form and hide bottom sheet
                        createTaskBinding.etTaskTitle.text?.clear()
                        createTaskBinding.etDetails.text?.clear()
                        chipGroup.clearCheck()
                        Toast.makeText(applicationContext, "Task added successfully", Toast.LENGTH_SHORT).show()
                        hideBottomSheetTask()
                    } else {
                        Toast.makeText(applicationContext, "Failed to add task", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupBottomSheetNote() {
        // Get the bottom sheet view
        val bottomSheetView = findViewById<NestedScrollView>(R.id.bottomSheetNote)

        // Initialize BottomSheetBehavior
        noteBottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView)

        // Configure behavior
        noteBottomSheetBehavior.apply {
            isHideable = true
            state = BottomSheetBehavior.STATE_HIDDEN

            // Set half-expanded ratio (50% of screen)
            isFitToContents = false
            halfExpandedRatio = 0.6f

            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_HIDDEN -> {
                            hideBottomSheetNote()
                        }
                        BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                            binding.root.setOnClickListener() {
                                hideBottomSheetNote()
                            }
                        }
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            binding.root.setOnClickListener() {
                                hideBottomSheetNote()
                            }
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                }
            })
        }
        createNoteBinding = NoteFormBinding.bind(bottomSheetView)

    }

    private fun setupNoteForm() {
        val repository = NoteRepository()

        // Get save button reference
        val saveButton = createNoteBinding.btnSave



        saveButton.setOnClickListener {
            // Get form values
            val title = createNoteBinding.etNoteTitle.text.toString().trim()
            val details = createNoteBinding.etDetails.text.toString().trim()


            // Validate input
            if (title.isEmpty()) {
                createNoteBinding.etNoteTitle.error = "Title is required"
                return@setOnClickListener
            }


            // Create Task object
            val note = Note(
                title = title,
                details = details,
                creationDate = Timestamp.now(),
            )

            hideBottomSheetNote()

            // Save to Firestore
            lifecycleScope.launch {
                try {
                    val result = repository.addNote(note)
                    if (result.isSuccess) {
                        // Clear form and hide bottom sheet
                        createNoteBinding.etNoteTitle.text?.clear()
                        createNoteBinding.etDetails.text?.clear()
                        Toast.makeText(applicationContext, "Note added successfully", Toast.LENGTH_SHORT).show()
                        hideBottomSheetNote()
                    } else {
                        Toast.makeText(applicationContext, "Failed to add note", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Method to show task bottom sheet
    private fun showBottomSheetTask() {
        taskBottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    // Method to hide task bottom sheet
    private fun hideBottomSheetTask() {
        taskBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    // Method to show note bottom sheet
    private fun showBottomSheetNote() {
        noteBottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    // Method to hide note bottom sheet
    private fun hideBottomSheetNote() {
        noteBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }
}